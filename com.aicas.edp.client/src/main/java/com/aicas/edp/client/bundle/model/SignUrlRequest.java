/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.bundle.model;


import lombok.ToString;

@ToString
public class SignUrlRequest
{
  public String symbolicName;
  public String version;
  public Boolean multicore;
  public String target;
  public Integer jaraABINumber;
}
