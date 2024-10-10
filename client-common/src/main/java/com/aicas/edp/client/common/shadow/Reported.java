/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

import com.aicas.edp.client.common.bundle.BundleInfo;
import com.aicas.edp.client.common.task.TaskResult;

import java.util.List;
import java.util.Map;

public class Reported
{
  public Boolean online;
  public String arch;
  public String osName;
  public String osVersion;
  public String javaRuntimeName;
  public String javaRuntimeVersion;
  public String vmAccelerationInfo;
  private List<BundleInfo> bundles;
  public Map<String, TaskResult> taskResults;

  public List<BundleInfo> getBundles()
  {
    return bundles;
  }

  public void setBundles(List<BundleInfo> bundles)
  {
    this.bundles = bundles;
  }
}
