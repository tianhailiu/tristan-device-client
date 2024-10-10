/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import java.util.Hashtable;

public class ConfigTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.CONFIG_TASK_RESULT;
  private Hashtable<String, Object> properties;

  public Hashtable<String, Object> getProperties()
  {
    return properties;
  }

  public void setProperties(Hashtable<String, Object> properties)
  {
    this.properties = properties;
  }

  @Override
  public TaskResultType getType()
  {
    return type;
  }
}
