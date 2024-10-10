/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.job.model.DescribeJobRequest;
import com.aicas.edp.client.job.model.JobDescription;
import com.aicas.edp.client.job.model.JobPayload;
import com.aicas.edp.client.job.model.JobStatus;
import com.aicas.edp.client.job.model.UpdateJobRequest;
import com.aicas.edp.client.util.AsyncIotMsg;
import com.aicas.edp.client.util.Context;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.aicas.edp.client.job.JobParser.parseJobDescription;

@Slf4j
@RequiredArgsConstructor
class JobHandler
{
  private static volatile boolean busy = false;
  private static final String UPDATE_JOB_POSTFIX_1 = "/jobs/";
  private static final String UPDATE_JOB_POSTFIX = "/update";
  private static final String GET_NEXT_JOB_POSTFIX = "/jobs/$next/get";
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final String UPDATE_JOB_PREFIX;
  private final String GET_NEXT_JOB_TOPIC;
  private final Context context;
  private final JobValidator jobValidator;
  private final String clientToken;
  private final ObjectMapper mapper = new ObjectMapper();

  JobHandler(Context context)
  {
    this.context = context;
    this.jobValidator = new JobValidator(context.getTaskManager());
    String thingName = context.getConfiguration().getThingName();
    clientToken = thingName;
    UPDATE_JOB_PREFIX = JobManager.PREFIX + thingName + UPDATE_JOB_POSTFIX_1;
    GET_NEXT_JOB_TOPIC = JobManager.PREFIX + thingName + GET_NEXT_JOB_POSTFIX;
  }

  void shutdown()
  {
    executor.shutdownNow();
  }

  void getNextJobRequest()
  {
    try
    {
      String payload =
        mapper.writeValueAsString(new DescribeJobRequest(clientToken));
      log.info("describing job {} execution", payload);
      AWSIotMessage msg =
        new AsyncIotMsg(GET_NEXT_JOB_TOPIC, AWSIotQos.QOS0, payload);
      context.getIotManager().publish(msg);
    }
    catch (AWSIotException e)
    {
      log.error("Failed to publish DescribeJobRequest", e);
    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to serialize DescribeJobRequest", e);
    }
    catch (Exception e)
    {
      log.error("Failed to request next Job", e);
    }
  }


  private void handleJobDescription(String payload)
  {
    if (busy)
    {
      log.debug("Describe Job response ignored because JobManager is busy");
      return;
    }
    JobDescription jd = parseJobDescription(payload);
    if (jd == null)
    {
      return; // Parsing failed, errors already logged, just stop processing
    }
    busy = true;
    String jobId = jd.execution.jobId;
    if (jd.execution.status.equals(
      JobStatus.QUEUED) || jd.execution.status.equals(JobStatus.IN_PROGRESS))
    {
      if (jobValidator.isValidJobDescription(jd))
      {
        log.debug("Job document is valid, starting Job #{}", jobId);
        if (jd.execution.status.equals(JobStatus.QUEUED))
          updateJobExecution(jobId, JobStatus.IN_PROGRESS);
        if (executeJob(jd.execution.jobPayload))
        {
          updateJobExecution(jobId, JobStatus.SUCCEEDED);
        }
        else
        {
          updateJobExecution(jobId, JobStatus.FAILED);
        }
      }
      else
      {
        log.debug("JobDocument validation failed, rejecting Job #{}", jobId);
        updateJobExecution(jobId, JobStatus.REJECTED);
      }
    }
    busy = false;
    getNextJobRequest();
  }

  /**
   * Helper blocking method, executes a list of Tasks sequentially.
   *
   * @param jobDocument Job Document that contains tasks
   * @return whether Job execution succeeded or not
   */
  private boolean executeJob(JobPayload jobDocument)
  {
    try
    {
      List<TaskResult> results =
        context.getTaskManager().executeAsync(jobDocument.tasks).join();
      log.debug("Job execution is finished {}", results);
      return results.stream()
        .allMatch(tr -> TaskStatus.SUCCEEDED.equals(tr.getStatus()));
    }
    catch (Exception e)
    {
      log.error("Job execution failed", e);
      return false;
    }
  }


  /**
   * Helper method that updates Job Execution status for current device
   * @param jobId ID of the Job to update status of
   * @param status New Job Execution status
   */
  private void updateJobExecution(String jobId, JobStatus status)
  {
    try
    {
      String payload = mapper.writeValueAsString(new UpdateJobRequest(status));
      log.info("updating job {} execution", payload);
      AWSIotMessage msg = new AsyncIotMsg(
        UPDATE_JOB_PREFIX + jobId + UPDATE_JOB_POSTFIX, AWSIotQos.QOS0,
        payload);
      context.getIotManager().publish(msg);
    }
    catch (AWSIotException e)
    {
      log.error("Failed to publish Job update request", e);
    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to serialize Job update document", e);
    }
    catch (Exception e)
    {
      log.error("Failed to update Job", e);
    }
  }

  public void handleJobDescriptionAsync(String stringPayload)
  {
    executor.submit(() -> handleJobDescription(stringPayload));
  }
}
