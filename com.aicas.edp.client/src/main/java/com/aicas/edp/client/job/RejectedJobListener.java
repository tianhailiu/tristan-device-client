/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import lombok.extern.slf4j.Slf4j;

/*
 * Listens for rejected topics of DescribeJob, and UpdateJob APIs, to log errors if any.
 */
@Slf4j
public class RejectedJobListener extends AWSIotTopic
{
  private final String topic;

  public RejectedJobListener(String thingName)
  {
    super("$aws/things/" + thingName + "/jobs/+/+/rejected", AWSIotQos.QOS0);
    topic = "$aws/things/" + thingName + "/jobs/+/+/rejected";
  }

  @Override
  public void onMessage(AWSIotMessage message)
  {
    log.error("Rejected: [{}] - {}", message.getTopic(),
              message.getStringPayload());
  }

  @Override
  public void onFailure()
  {
    super.onFailure();
    log.error("Async subscription failed for topic '{}'", topic);
  }
}
