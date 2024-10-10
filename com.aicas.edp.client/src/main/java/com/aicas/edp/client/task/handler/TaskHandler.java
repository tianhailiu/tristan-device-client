/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.task.handler;

import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;

public interface TaskHandler
{

  TaskResult execute(Task task);

  boolean isValid(Task task);
}
