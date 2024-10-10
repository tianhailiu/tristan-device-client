/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import lombok.Builder;

public class CertInfo
{
  public final String organization;
  public final String environment;
  public final String thingName;
  public final String deviceType;
  public final String topicPrefix;

  @Builder
  public CertInfo(String organization, String environment, String thingName,
                  String deviceType)
  {
    this.organization = organization;
    this.environment = environment;
    this.thingName = thingName;
    this.deviceType = deviceType;
    this.topicPrefix = organization + "/" + environment + "/" + thingName;
  }
}
