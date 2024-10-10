/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DeployTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.DEPLOY_TASK_RESULT;
  private List<String> unsatisfied;

  @Override
  public TaskResultType getType()
  {
    return type;
  }

  @JsonIgnore
  public List<String> getUnsatisfied()
  {
    return unsatisfied;
  }

  @JsonIgnore
  public void setUnsatisfied(List<String> unsatisfied)
  {
    this.unsatisfied = unsatisfied;
  }

  @JsonProperty("unsatisfied")
  public String[] getUnsatisfiedArray()
  {
    if (unsatisfied == null)
      return null;
    return unsatisfied.toArray(new String[0]);
  }
}
