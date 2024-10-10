/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

import com.aicas.edp.client.common.task.Task;

import java.util.HashMap;
import java.util.Map;

public class Desired
{
  public Map<String, Task> tasks = new HashMap<>();
}
