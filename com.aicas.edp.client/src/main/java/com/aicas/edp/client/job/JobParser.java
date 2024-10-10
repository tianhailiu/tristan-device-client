/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.aicas.edp.client.job.model.JobDescription;
import com.aicas.edp.client.job.model.JobPayload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobParser
{
  private final static ObjectMapper mapper = new ObjectMapper();

  public static JobDescription parseJobDescription(String payload)
  {
    try
    {
      JobDescription jd = mapper.readValue(payload, JobDescription.class);
      if (jd.execution == null)
      {
        return null;
      }
      if (jd.execution.jobId == null || jd.execution.jobId.length() < 1)
      {
        log.error("JobId is empty");
        return null;
      }
      jd.execution.jobPayload = parseJobPayload(jd.execution.jobDocument);
      return jd;
    }
    catch (Exception e)
    {
      log.error("Error during parsing AWS job payload from JSON.", e);
      return null;
    }
  }

  private static JobPayload parseJobPayload(JsonNode jobDocument)
  {
    try
    {
      return mapper.readValue(jobDocument.toString(), JobPayload.class);
    }
    catch (Exception e)
    {
      log.error("Failed to parse JobDocument: {}", jobDocument.toString(), e);
      return null;
    }
  }
}
