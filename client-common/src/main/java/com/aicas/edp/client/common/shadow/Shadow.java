/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;


public class Shadow
{
  public State state = new State();
  public Metadata metadata = new Metadata();
  public Long version;
  public Long timestamp;
  public String clientToken;
}

