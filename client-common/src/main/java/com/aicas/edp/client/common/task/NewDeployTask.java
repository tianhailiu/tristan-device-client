/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import java.util.List;

public class NewDeployTask extends Task
{
  public static final TaskType type = TaskType.NEW_DEPLOY_TASK;
  public List<InstallBundle> installs;

  @Override
  public TaskType getType()
  {
    return type;
  }
}
