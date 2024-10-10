/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskResultType
{
  BASIC_TASK_RESULT(Constants.BASIC_TASK_RESULT),
  CONFIG_TASK_RESULT(Constants.CONFIG_TASK_RESULT),
  DEPLOY_TASK_RESULT(Constants.DEPLOY_TASK_RESULT),
  BUNDLE_TASK_RESULT(Constants.BUNDLE_TASK_RESULT),
  LAUNCH_TASK_RESULT(Constants.LAUNCH_TASK_RESULT),
  SENSOR_TASK_RESULT(Constants.SENSOR_TASK_RESULT);

  public final String name;

  TaskResultType(String name)
  {
    this.name = name;
  }

  @Override
  public String toString()
  {
    return name;
  }

  @JsonValue
  public String getName()
  {
    return name;
  }

  public static class Constants
  {
    public static final String BASIC_TASK_RESULT = "basic-task-result";
    public static final String CONFIG_TASK_RESULT = "config-task-result";
    public static final String BUNDLE_TASK_RESULT = "bundle-task-result";
    public static final String LAUNCH_TASK_RESULT = "launch-task-result";
    public static final String SENSOR_TASK_RESULT = "sensor-task-result";
    public static final String DEPLOY_TASK_RESULT = "deploy-task-result";
  }
}
