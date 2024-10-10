/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LaunchTask extends Task
{
  public static final TaskType type = TaskType.LAUNCH_TASK;
  private String symbolicName;
  private String version;

  @Override
  public TaskType getType()
  {
    return type;
  }
}
