/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import com.aicas.edp.client.Activator;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.security.KeyStore;
import java.util.Map;

import static com.aicas.edp.client.util.Util.fileToURL;
import static com.aicas.edp.client.util.Util.notEmpty;
import static com.aicas.edp.client.util.Util.parseCertInfo;
import static com.aicas.edp.client.util.Util.readKeyStore;

/**
 * Manager of all related to EDP Client configuration
 */
@Slf4j
public class Configuration
{
  private final static String KEYSTORE = "edp.client.keystore";
  private final static String KEYSTORE_PASS = "edp.client.keystore-password";
  private final static String ENDPOINT = "edp.client.iot-endpoint";
  private final Map<String, String> properties;
  private final KeyStore keyStore;
  private final CertInfo certInfo;

  public Configuration(Map<String, String> properties)
  {
    this.properties = properties;
    keyStore = readKeyStore(getKeystoreURL(), getKeystorePassword());
    certInfo = parseCertInfo(keyStore);
    log.trace("Configuration Manager created");
  }

  public String getEndpoint()
  {
    log.trace("Reading endpoint from framework properties");
    String endpoint = System.getProperty(ENDPOINT);

    if (Util.isEmpty(endpoint))
    {
      log.trace("Endpoint not found in system properties");
      log.trace("Reading endpoint from classpath properties");
      endpoint = properties.get(ENDPOINT);
    }
    log.debug("Endpoint is: {}", endpoint);
    return endpoint;
  }

  /**
   * Returns keystore file URL, looks into system properties and then in classpath
   * @return Keystore File URL
   */
  public URL getKeystoreURL()
  {
    URL keystore;
    // First check if keystore file is provided via system properties.
    String extFilename = System.getProperty(KEYSTORE);
    // First check if keystore is configured via system properties
    if (notEmpty(extFilename))
    {
      log.debug("External keystore is set in system properties: {}",
                extFilename);
      keystore = fileToURL(extFilename);

      if (keystore == null)
      {
        log.warn("External keystore {} is invalid file", extFilename);
      }
      return keystore;
    }
    // If we are here, it means no keystore in system properties, let's try classpath
    keystore =
      Activator.class.getClassLoader().getResource(properties.get(KEYSTORE));
    if (keystore == null)
    {
      throw new RuntimeException("KeyStore not found");
    }
    return keystore;
  }

  public String getKeystorePassword()
  {
    log.trace("Reading keystore password");
    String password = System.getProperty(KEYSTORE_PASS);
    if (notEmpty(password))
    {
      log.debug("Found password in system properties");
    }
    else
    {
      log.debug(
        "External password not provided, looking in classpath properties");
      password = properties.get(KEYSTORE_PASS);
    }
    if (password == null)
    {
      log.debug(
        "Password not provided, assuming keystore is not password protected");
      password = "";
    }
    return password;
  }

  public String getCommonTopicPrefix()
  {
    return certInfo.topicPrefix;
  }

  public KeyStore getKeyStore()
  {
    return keyStore;
  }

  public String getThingName()
  {
    return this.certInfo.thingName;
  }


}
