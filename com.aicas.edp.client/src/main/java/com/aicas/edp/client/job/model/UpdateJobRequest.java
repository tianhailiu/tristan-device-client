/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateJobRequest
{
  public JobStatus status;
  public Map<String, String> statusDetails;
  public Long expectedVersion;
  public Long executionNumber;
  public boolean includeJobExecutionState;
  public boolean includeJobDocument;
  public Long stepTimeoutInMinutes;
  public String clientToken;

  public UpdateJobRequest(JobStatus status)
  {
    this.status = status;
  }
}
