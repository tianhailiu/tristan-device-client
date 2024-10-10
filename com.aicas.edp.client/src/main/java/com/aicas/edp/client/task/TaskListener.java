/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.aicas.edp.client.task;

import com.aicas.edp.client.common.task.BasicTaskResult;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.util.Context;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * This class extends {@link AWSIotTopic} to receive messages from a subscribed
 * topic.
 */
@Slf4j
public class TaskListener extends AWSIotTopic
{
  private final Context context;
  private final TaskManager taskManager;
  private final String prefix;
  private final ObjectMapper mapper;

  public TaskListener(Context context, TaskManager taskManager)
  {
    super(context.getConfiguration().getCommonTopicPrefix() + "/task/request",
          AWSIotQos.QOS0);
    prefix = context.getConfiguration().getCommonTopicPrefix();
    this.context = context;
    this.taskManager = taskManager;

    mapper = new ObjectMapper();
    // TODO migrate to activateDefaultTyping
    mapper.enableDefaultTyping();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  @Override
  public void onMessage(AWSIotMessage message)
  {
    try
    {
      Task task = mapper.readValue(message.getStringPayload(), Task.class);
      taskManager.executeAsync(task).whenComplete((result, ex) -> {
        if (result == null)
        {
          log.error("Task execution failed, task: {}", task, ex);
          result = new BasicTaskResult();
          result.setTaskId(task.getId());
          result.setStatus(TaskStatus.FAILED);
          result.setMessage(
            "Unexpected exception occurred during task execution");
        }
        sendTaskResponse(result);
      });
    }
    catch (JsonProcessingException jsm)
    {
      log.error("Failed to parse task json '{}'", message.getStringPayload(),
                jsm);
    }
    catch (Exception e)
    {
      log.error("Unexpected error", e);
    }
  }

  private void sendTaskResponse(TaskResult taskResult)
  {
    try
    {
      String json = mapper.writeValueAsString(taskResult);
      AWSIotMessage awsIotMessage =
        new AWSIotMessage(prefix + "/task/response", AWSIotQos.QOS0, json);
      context.getIotManager().publish(awsIotMessage);

    }
    catch (AWSIotException e)
    {
      log.error("Failed to send response", e);
    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to serialize task result: {}", taskResult, e);
    }
  }


}
