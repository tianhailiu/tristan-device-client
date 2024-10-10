/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

public class DesiredState
{
  public Desired desired = new Desired();

  public DesiredState()
  {
  }

  public DesiredState(State state)
  {
    desired = state.desired;
  }
}
