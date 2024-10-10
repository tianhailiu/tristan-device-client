/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorTask extends Task
{
  private static final TaskType type = TaskType.SENSOR_TASK;
  public ActionType action;
  public String config;

  @Override
  public TaskType getType()
  {
    return type;
  }

  public enum ActionType
  {
    LIST(Constants.LIST),
    START(Constants.START),
    STOP(Constants.STOP);

    public final String type;

    ActionType(String name)
    {
      this.type = name;
    }

    @Override
    public String toString()
    {
      return type;
    }

    @JsonValue
    public String getType()
    {
      return type;
    }

    public static class Constants
    {
      public static final String START = "START";
      public static final String STOP = "STOP";
      public static final String LIST = "LIST";
    }
  }
}
