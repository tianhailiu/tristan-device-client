/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

@Deprecated
public class DeployTask extends Task
{
  public static final TaskType type = TaskType.DEPLOY_TASK;
  public DeploymentPlan deploymentPlan;

  @Override
  public TaskType getType()
  {
    return type;
  }
}
