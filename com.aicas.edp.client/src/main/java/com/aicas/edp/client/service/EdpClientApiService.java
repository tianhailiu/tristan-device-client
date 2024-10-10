/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.service;

import com.aicas.edp.client.api.ConnectionState;
import com.aicas.edp.client.api.EdpClientAPI;
import com.aicas.edp.client.util.Context;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdpClientApiService implements EdpClientAPI
{
  private final Context context;

  public EdpClientApiService(Context context)
  {
    this.context = context;
    log.trace("EdpClientApiService created");
  }

  public void init()
  {
    try
    {
      context.getBundleContext()
        .registerService(EdpClientAPI.class, this, null);
    }
    catch (Exception e)
    {
      log.error("Failed to register service EDPClientAPI", e);
    }
  }

  @Override
  public ConnectionState isConnected()
  {
    return ConnectionState.valueOf(
      context.getIotManager().getConnectionStatus().name());
  }
}
