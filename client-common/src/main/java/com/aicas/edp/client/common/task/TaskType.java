/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskType
{
  BUNDLE_TASK(Constants.BUNDLE_TASK),
  NEW_DEPLOY_TASK(Constants.NEW_DEPLOY_TASK),
  DEPLOY_TASK(Constants.DEPLOY_TASK),
  CONFIG_TASK(Constants.CONFIG_TASK),
  LAUNCH_TASK(Constants.LAUNCH_TASK),
  SENSOR_TASK(Constants.SENSOR_TASK);

  public final String name;

  TaskType(String name)
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
    public static final String BUNDLE_TASK = "bundle-task";
    public static final String NEW_DEPLOY_TASK = "new-deploy-task";
    public static final String CONFIG_TASK = "config-task";
    public static final String LAUNCH_TASK = "launch-task";
    public static final String SENSOR_TASK = "sensor-task";
    public static final String DEPLOY_TASK = "deploy-task";
  }
}
