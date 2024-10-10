/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.bundle;

import java.util.HashMap;

@SuppressWarnings("unused")
public enum BundleEventEnum
{
  INSTALLED(1),
  STARTED(2),
  STOPPED(4),
  UPDATED(8),
  UNINSTALLED(16),
  RESOLVED(32),
  UNRESOLVED(64),
  STARTING(128),
  STOPPING(256),
  LAZY_ACTIVATION(512);

  private Integer code;
  private static HashMap<Integer, BundleEventEnum> bundleEventTypeMap =
    new HashMap<>();

  static
  {
    for (BundleEventEnum bundleEventEnum : BundleEventEnum.values())
    {
      bundleEventTypeMap.put(bundleEventEnum.code, bundleEventEnum);
    }
  }

  BundleEventEnum(Integer code)
  {
    this.code = code;
  }

  public static BundleEventEnum get(Integer code)
  {
    return bundleEventTypeMap.get(code);
  }

  public Integer code()
  {
    return code;
  }
}
