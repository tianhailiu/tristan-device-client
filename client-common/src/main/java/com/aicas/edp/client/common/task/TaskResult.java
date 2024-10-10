/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;

import java.time.LocalDateTime;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
  @Type(value = BundleTaskResult.class, name = TaskResultType.Constants.BUNDLE_TASK_RESULT),
  @Type(value = DeployTaskResult.class, name = TaskResultType.Constants.DEPLOY_TASK_RESULT),
  @Type(value = ConfigTaskResult.class, name = TaskResultType.Constants.CONFIG_TASK_RESULT),
  @Type(value = LaunchTaskResult.class, name = TaskResultType.Constants.LAUNCH_TASK_RESULT),
  @Type(value = SensorTaskResult.class, name = TaskResultType.Constants.SENSOR_TASK_RESULT),
  @Type(value = BasicTaskResult.class, name = TaskResultType.Constants.BASIC_TASK_RESULT)

})
@ToString
public abstract class TaskResult
{
  private TaskResultType type;
  private String taskId;
  private TaskStatus status;
  private String message;
  private LocalDateTime started;
  private LocalDateTime finished;

  public TaskStatus getStatus()
  {
    return status;
  }

  public void setStatus(TaskStatus status)
  {
    this.status = status;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }

  public LocalDateTime getStarted()
  {
    return started;
  }

  public void setStarted(LocalDateTime started)
  {
    this.started = started;
  }

  public LocalDateTime getFinished()
  {
    return finished;
  }

  public void setFinished(LocalDateTime finished)
  {
    this.finished = finished;
  }

  public String getTaskId()
  {
    return taskId;
  }

  public void setTaskId(String taskId)
  {
    this.taskId = taskId;
  }

  public TaskResultType getType()
  {
    return type;
  }
}
