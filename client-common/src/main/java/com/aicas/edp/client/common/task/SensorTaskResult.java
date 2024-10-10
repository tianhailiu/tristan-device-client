/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

public class SensorTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.SENSOR_TASK_RESULT;
  public String config;
  public String sensors;

  @Override
  public TaskResultType getType()
  {
    return type;
  }

  public String getConfig()
  {
    return config;
  }

  public void setConfig(String config)
  {
    this.config = config;
  }

  public String getSensors()
  {
    return sensors;
  }

  public void setSensors(String sensors)
  {
    this.sensors = sensors;
  }
}
