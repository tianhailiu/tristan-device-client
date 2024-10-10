/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BundleTaskResult extends TaskResult
{
  public static final TaskResultType type = TaskResultType.BUNDLE_TASK_RESULT;
  private List<String> bundles;

  @Override
  public TaskResultType getType()
  {
    return type;
  }

  @JsonIgnore
  public List<String> getBundles()
  {
    return bundles;
  }

  @JsonIgnore
  public void setBundles(List<String> bundles)
  {
    this.bundles = bundles;
  }

  @JsonProperty("bundles")
  public String[] getBundleArray()
  {
    if (bundles == null)
      return null;
    return bundles.toArray(new String[0]);
  }
}
