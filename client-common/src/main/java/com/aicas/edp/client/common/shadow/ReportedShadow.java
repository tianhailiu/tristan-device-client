/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

public class ReportedShadow
{
  public ReportedState state = new ReportedState();
  //public Long version;

  public ReportedShadow()
  {
  }

  public ReportedShadow(Shadow shadow)
  {
    state = new ReportedState(shadow.state);
    //version = shadow.version;
  }
}
