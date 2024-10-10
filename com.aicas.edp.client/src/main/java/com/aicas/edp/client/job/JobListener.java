/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobListener extends AWSIotTopic
{
  private final JobManager jobManager;

  public JobListener(JobManager jobManager, String topic)
  {
    super(topic, AWSIotQos.QOS0);
    this.jobManager = jobManager;
  }

  @Override
  public void onMessage(AWSIotMessage message)
  {
    log.debug("Received message from topic '{}', body: {}", message.getTopic(),
              message.getStringPayload());
    jobManager.handleJobDescriptionAsync(message.getStringPayload());
  }

  @Override
  public void onFailure()
  {
    super.onFailure();
    log.error("Error code: {}, error message: {}   ", this.getErrorCode(),
              this.getErrorMessage());
  }
}
