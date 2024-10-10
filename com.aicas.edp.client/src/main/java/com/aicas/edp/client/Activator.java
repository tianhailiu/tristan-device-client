/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client;

import com.aicas.edp.client.util.Context;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class Activator implements BundleActivator
{
  private final Map<String, String> properties;
  private Context context;

  public Activator()
  {
    properties = readProperties();
  }

  @Override
  public void start(BundleContext bundleContext)
    throws
    Exception
  {
    context = new Context(properties, bundleContext);
    context.initialize();
    context.connect();
  }

  @Override
  public void stop(BundleContext bundleContext)
    throws
    Exception
  {
    context.disconnect();
    context.shutdown();
  }

  private Map<String, String> readProperties()
  {
    Map<String, String> properties;
    try (InputStream input = this.getClass().getClassLoader()
      .getResourceAsStream("config.properties"))
    {
      if (input == null)
      {
        log.error("config.properties not found in class path");
        properties = new HashMap<>();
      }
      else
      {
        Properties props = new Properties();
        props.load(input);
        properties = propertiesToMap(props);
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    return properties;
  }

  private HashMap<String, String> propertiesToMap(Properties prop)
  {
    return prop.entrySet().stream().collect(
      Collectors.toMap(
        e -> String.valueOf(e.getKey()),
        e -> String.valueOf(e.getValue()),
        (prev, next) -> next, HashMap::new
      ));
  }
}
