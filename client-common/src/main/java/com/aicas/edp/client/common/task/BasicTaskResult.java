/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

public class BasicTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.BASIC_TASK_RESULT;

  @Override
  public TaskResultType getType()
  {
    return type;
  }
}
