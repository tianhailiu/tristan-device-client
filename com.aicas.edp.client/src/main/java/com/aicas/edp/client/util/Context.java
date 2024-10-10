/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import com.aicas.edp.client.bundle.BundleManager;
import com.aicas.edp.client.iot.IotManager;
import com.aicas.edp.client.job.JobManager;
import com.aicas.edp.client.service.EdpClientApiService;
import com.aicas.edp.client.shadow.ShadowManager;
import com.aicas.edp.client.task.TaskManager;
import com.amazonaws.services.iot.client.AWSIotException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;

import java.util.Map;

@Slf4j
public class Context
{
  private final BundleContext bundleContext;
  private final Configuration configuration;
  private final IotManager iotManager;
  private final TaskManager taskManager;
  private final ShadowManager shadowManager;
  private final BundleManager bundleManager;
  private final JobManager jobManager;
  private final EdpClientApiService edpClientApi;

  public Context(Map<String, String> properties, BundleContext bundleContext)
  {
    this.bundleContext = bundleContext;
    this.configuration = new Configuration(properties);

    iotManager = new IotManager(this);
    taskManager = new TaskManager(this);
    jobManager = new JobManager(this);
    shadowManager = new ShadowManager(this);
    bundleManager = new BundleManager(this);

    this.edpClientApi = new EdpClientApiService(this);
  }

  public void initialize()
  {
    log.trace("initializing");
    iotManager.initialize();
    shadowManager.initialize();
    bundleManager.initialize();
    taskManager.initialize();
    jobManager.initialize();
    edpClientApi.init();
  }

  public void connect()
    throws
    AWSIotException
  {
    iotManager.connect();
    shadowManager.connect();
    bundleManager.connect();
    taskManager.connect();
    jobManager.connect();
  }

  public void disconnect()
    throws
    AWSIotException
  {
    jobManager.disconnect();
    taskManager.disconnect();
    bundleManager.disconnect();
    shadowManager.disconnect();
    iotManager.disconnect();
  }

  public void shutdown()
  {
    jobManager.shutdown();
    taskManager.shutdown();
    bundleManager.shutdown();
    shadowManager.shutdown();
    iotManager.shutdown();
  }

  public Configuration getConfiguration()
  {
    return this.configuration;
  }

  public TaskManager getTaskManager()
  {
    return taskManager;
  }

  public IotManager getIotManager()
  {
    return iotManager;
  }

  public BundleContext getBundleContext()
  {
    return bundleContext;
  }

  public BundleManager getBundleManager()
  {
    return bundleManager;
  }

  public ShadowManager getShadowManager()
  {
    return shadowManager;
  }


}
