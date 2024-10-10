/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LaunchTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.LAUNCH_TASK_RESULT;
  private List<String> results;

  @Override
  public TaskResultType getType()
  {
    return type;
  }

  @JsonIgnore
  public List<String> getResults()
  {
    return results;
  }

  @JsonIgnore
  public void setResults(List<String> bundles)
  {
    this.results = bundles;
  }

  @JsonProperty("results")
  public String[] getResultsArray()
  {
    if (results == null)
      return null;
    return results.toArray(new String[0]);
  }
}
