/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.iot;

import com.aicas.edp.client.shadow.IotDevice;
import com.aicas.edp.client.util.Configuration;
import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.Manager;
import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import lombok.extern.slf4j.Slf4j;

import java.security.KeyStore;

import static com.aicas.edp.client.util.Util.createWillMessage;

@Slf4j
public class IotManager implements Manager
{
  private final Context context;
  private IotClient iotClient;

  public IotManager(Context context)
  {
    this.context = context;
    log.trace("Iot Manager created");
  }

  @Override
  public void initialize()
  {
    Configuration cfg = context.getConfiguration();
    String endpoint = cfg.getEndpoint();
    String thingId = cfg.getThingName();
    KeyStore keyStore = cfg.getKeyStore();
    String keyStorePwd = cfg.getKeystorePassword();
    iotClient = new IotClient(endpoint, thingId, keyStore, keyStorePwd);
    iotClient.setMaxConnectionRetries(Integer.MAX_VALUE);
    iotClient.setMaxRetryDelay(5 * 60 * 1000);
    iotClient.setWillMessage(createWillMessage(cfg.getThingName()));
    log.trace("Iot Manager initialized");
  }

  @Override
  public void connect()
    throws
    AWSIotException
  {
    try
    {
      iotClient.connect(0, false);
    }
    catch (AWSIotTimeoutException e)
    {
      // we should not catch timeout exception since there is no timeout
    }
    log.trace("Iot Manager is connected: {}", iotClient.getConnectionStatus());
  }

  @Override
  public void disconnect()
    throws
    AWSIotException
  {
    try
    {
      iotClient.disconnect(0, false);
    }
    catch (AWSIotTimeoutException e)
    {
      // we should not catch timeout exception since there is no timeout
    }
    log.trace("Iot Manager is being deactivated");
  }

  @Override
  public void shutdown()
  {
  }

  public void publish(AWSIotMessage message)
    throws
    AWSIotException
  {
    iotClient.publish(message);
  }

  public void subscribe(AWSIotTopic subscriber)
    throws
    AWSIotException
  {
    iotClient.subscribe(subscriber);
  }

  public void unsubscribe(AWSIotTopic subscriber)
    throws
    AWSIotException
  {
    iotClient.unsubscribe(subscriber);
  }

  public void registerConnectionSuccessHandler(Runnable onConnectionSuccess)
  {
    this.iotClient.registerConnectionSuccessHandler(onConnectionSuccess);
  }

  public void attach(IotDevice iotDevice)
    throws
    AWSIotException
  {
    iotClient.attach(iotDevice);
  }

  public void detach(IotDevice iotDevice)
    throws
    AWSIotException
  {
    iotClient.detach(iotDevice);
  }

  public AWSIotConnectionStatus getConnectionStatus()
  {
    return iotClient.getConnectionStatus();
  }
}
