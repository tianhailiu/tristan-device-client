/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class JobExecution
{
  public String jobId;
  public String thingName;
  public JsonNode jobDocument;
  @JsonIgnore
  public JobPayload jobPayload;
  public JobStatus status;
  public Map<String, String> statusDetails;
  public Long queuedAt;
  public Long startedAt;
  public Long lastUpdatedAt;
  public Long versionNumber;
  public Long executionNumber;

}
