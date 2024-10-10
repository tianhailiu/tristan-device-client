/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.shadow;

import com.amazonaws.services.iot.client.AWSIotDeviceErrorCode;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetShadowMessage extends AWSIotMessage
{
  private final IotDevice iotDevice;

  public GetShadowMessage(IotDevice iotDevice)
  {
    super(null, AWSIotQos.QOS0);
    this.iotDevice = iotDevice;
  }

  @Override
  public void onSuccess()
  {
    log.debug("ShadowGet result: {}", this.getStringPayload());
    iotDevice.onShadowGetResult(this.getStringPayload());
  }

  @Override
  public void onFailure()
  {
    if (this.errorCode == AWSIotDeviceErrorCode.NOT_FOUND)
    {
      log.warn("Cloud shadow is absent");
    }
    else
    {
      log.error(
        "Failed to send get Shadow request. errorCode: {}, errorMessage: {}",
        this.getErrorCode(), this.getErrorMessage());
    }
  }

  @Override
  public void onTimeout()
  {
    log.error(
      "Failed to send get Shadow request. errorCode: {}, errorMessage: {}",
      this.getErrorCode(), this.getErrorMessage());
  }
}
