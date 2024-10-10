/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.aicas.edp.api_version.ApiParent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes(value = {
  @Type(value = BundleTask.class, name = TaskType.Constants.BUNDLE_TASK),
  @Type(value = NewDeployTask.class, name = TaskType.Constants.NEW_DEPLOY_TASK),
  @Type(value = DeployTask.class, name = TaskType.Constants.DEPLOY_TASK),
  @Type(value = ConfigTask.class, name = TaskType.Constants.CONFIG_TASK),
  @Type(value = LaunchTask.class, name = TaskType.Constants.LAUNCH_TASK),
  @Type(value = SensorTask.class, name = TaskType.Constants.SENSOR_TASK)
})
public abstract class Task extends ApiParent
{
  private String id;
  private Long timestamp = 0L;

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public abstract TaskType getType();

  public Long getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(Long timestamp)
  {
    this.timestamp = timestamp;
  }
}
