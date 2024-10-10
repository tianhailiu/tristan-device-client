/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

public class ReportedState
{
  public Reported reported = new Reported();

  public ReportedState()
  {
  }

  public ReportedState(State state)
  {
    reported = state.reported;
  }
}
