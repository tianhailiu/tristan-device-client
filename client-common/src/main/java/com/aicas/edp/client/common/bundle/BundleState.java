/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.bundle;

import java.util.HashMap;

public enum BundleState
{
  UNINSTALLED(1),
  INSTALLED(2),
  RESOLVED(4),
  STARTING(8),
  STOPPING(16),
  ACTIVE(32);

  private Integer code;
  private static HashMap<Integer, BundleState> bundleStateTypeMap =
    new HashMap<>();

  static
  {
    for (BundleState bundleState : BundleState.values())
    {
      bundleStateTypeMap.put(bundleState.code, bundleState);
    }
  }

  BundleState(Integer code)
  {
    this.code = code;
  }

  public static BundleState get(Integer code)
  {
    return bundleStateTypeMap.get(code);
  }

  public Integer code()
  {
    return code;
  }
}
