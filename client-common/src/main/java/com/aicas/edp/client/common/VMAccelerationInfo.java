/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common;

import java.util.Objects;

public class VMAccelerationInfo
{
  public Boolean multicore;
  public String target;
  public Integer jaraABINumber;

  /**
   * @return pattern of accelerator linux_x86_64_m1
   */
  @Override
  public String toString()
  {
    return String.format("%s_%s%d",
                         target,
                         Objects.nonNull(multicore) && multicore ? "m" : "s",
                         jaraABINumber);
  }
}
