/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescribeJobRequest
{
  public Long executionNumber;
  public Boolean includeJobDocument;
  public String clientToken;

  public DescribeJobRequest(String clientToken)
  {
    this.clientToken = clientToken;
  }
}
