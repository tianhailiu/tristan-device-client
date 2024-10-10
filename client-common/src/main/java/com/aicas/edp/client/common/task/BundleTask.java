/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import com.aicas.edp.client.common.bundle.BundleAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleTask extends Task
{
  public static final TaskType type = TaskType.BUNDLE_TASK;
  private String symbolicName;
  private String version;
  private BundleAction action;

  @Override
  public TaskType getType()
  {
    return type;
  }
}
