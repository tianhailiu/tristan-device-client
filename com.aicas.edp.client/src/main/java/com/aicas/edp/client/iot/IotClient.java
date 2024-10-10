/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.iot;

import com.amazonaws.services.iot.client.AWSIotMqttClient;

import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class IotClient extends AWSIotMqttClient
{
  private final List<Runnable> connSuccessHandlers =
    new CopyOnWriteArrayList<>();

  public IotClient(String clientEndpoint, String clientId, KeyStore keyStore,
                   String keyPassword)
  {
    super(clientEndpoint, clientId, keyStore, keyPassword);
  }

  @Override
  public void onConnectionSuccess()
  {
    super.onConnectionSuccess();
    connSuccessHandlers.forEach(Runnable::run);
  }

  public void registerConnectionSuccessHandler(Runnable runnable)
  {
    connSuccessHandlers.add(runnable);
  }
}
