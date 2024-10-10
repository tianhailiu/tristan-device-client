/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job.model;

public enum JobStatus
{
  QUEUED,
  IN_PROGRESS,
  FAILED,
  SUCCEEDED,
  CANCELED,
  TIMED_OUT,
  REJECTED,
  REMOVED
}
