/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Hashtable;

@Getter
@Setter
public class ConfigTask extends Task
{
  private static final TaskType type = TaskType.CONFIG_TASK;
  private Integer bundleId;
  private String pid;
  private String location;
  private Hashtable<String, Object> properties;

  @Override
  public TaskType getType()
  {
    return type;
  }
}
