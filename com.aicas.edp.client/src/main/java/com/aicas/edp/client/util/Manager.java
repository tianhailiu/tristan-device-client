/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import com.amazonaws.services.iot.client.AWSIotException;

public interface Manager
{

  void initialize();

  void connect()
    throws
    AWSIotException;

  void disconnect()
    throws
    AWSIotException;

  void shutdown();
}
