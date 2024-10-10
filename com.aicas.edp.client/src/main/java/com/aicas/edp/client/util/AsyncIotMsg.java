/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.aicas.edp.client.util;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncIotMsg extends AWSIotMessage
{

  public AsyncIotMsg(String topic, AWSIotQos qos, String payload)
  {
    super(topic, qos, payload);
  }

  @Override
  public void onSuccess()
  {
    log.debug("Published successfully to '{}', payload: '{}'", topic,
              getStringPayload());
  }

  @Override
  public void onFailure()
  {
    log.error("Publish failed to '{}', payload: '{}'", topic,
              getStringPayload());
  }

  @Override
  public void onTimeout()
  {
    log.error("Publish to '{}' has timed out, payload: '{}'", topic,
              getStringPayload());
  }

}
