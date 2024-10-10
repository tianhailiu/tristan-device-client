/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.aicas.edp.api_version.ApiVersionService;
import com.aicas.edp.api_version.UnsupportedVersionException;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.job.model.JobDescription;
import com.aicas.edp.client.job.model.JobPayload;
import com.aicas.edp.client.task.TaskManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JobValidator
{
  private final TaskManager taskManager;

  public boolean isValidJobDescription(JobDescription jobDescription)
  {
    try
    {
      JobPayload job = jobDescription.execution.jobPayload;
      if (job == null || job.tasks == null || job.tasks.isEmpty())
      {
        log.debug("Job is empty");
        return false;
      }
      Optional<Task> checkTaskVersion =
        job.tasks.stream().findAny().filter(task -> {
          try
          {
            //TODO fix this
            ApiVersionService apiVersionService = new ApiVersionService();
            apiVersionService.checkVersion(task);
          }
          catch (UnsupportedVersionException e)
          {
            log.error(
              "Invalid version for object {} ( supported: {}, actual: {} )"
              , e.getTargetClass().getName()
              , e.getSupportedVersion()
              , e.getActualVersion());
            return false;
          }
          return true;
        });
      if (!checkTaskVersion.isPresent())
        return false;
      if (job.tasks.stream().anyMatch(Objects::isNull))
      {
        log.error("Invalid Job, null task");
        return false;
      }
      if (!job.tasks.stream().allMatch(taskManager::isValid))
      {
        log.error("Task validation failed");
        return false;
      }
      return true;
    }
    catch (Exception e)
    {
      log.error("Failed to validate Job description", e);
      return false;
    }
  }
}
