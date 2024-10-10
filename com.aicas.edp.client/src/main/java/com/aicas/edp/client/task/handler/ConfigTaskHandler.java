/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.task.handler;

import com.aicas.edp.client.common.task.ConfigTask;
import com.aicas.edp.client.common.task.ConfigTaskResult;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.util.Context;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleContext;

@Slf4j
public class ConfigTaskHandler implements TaskHandler
{
  private final BundleContext bndCtx;
  private final Context ctx;

  public ConfigTaskHandler(BundleContext bndCtx, Context ctx)
  {
    this.bndCtx = bndCtx;
    this.ctx = ctx;
  }

  @Override
  public boolean isValid(Task task)
  {
    if (!(task instanceof ConfigTask))
    {
      log.error("Validation failed: Task {} is not instance of ConfigTask",
                task.getClass());
      return false;
    }
    ConfigTask t = (ConfigTask) task;
    if (t.getBundleId() == null)
    {
      log.error("Validation failed: BundleId is null");
      return false;
    }
    if (t.getLocation() == null || t.getLocation().length() < 1)
    {
      log.error("Validation failed: Bundle location is empty '{}'",
                t.getLocation());
      return false;
    }
    if (t.getPid() == null || t.getPid().length() < 1)
    {
      log.error("Validation failed: Bundle pid is empty '{}'", t.getPid());
      return false;
    }
    return true;
  }

  @Override
  public TaskResult execute(Task task)
  {
    ConfigTaskResult result = new ConfigTaskResult();
    result.setTaskId(task.getId());

    if (!(task instanceof ConfigTask))
    {
      log.error("{} is not instance of ConfigTask", task.getClass());
      result.setStatus(TaskStatus.REJECTED);
      result.setMessage("Type mismatch, report to system administrator.");
      return result;
    }

        /*ConfigurationAdmin configurationAdmin = agent.configurationAdmin;
        if (configurationAdmin == null) {
            result.setStatus(TaskStatus.REJECTED);
            result.setMessage("No instance of ConfigurationAdmin present");
            return result;
        }

        ConfigTask configTask = (ConfigTask) task;
        String location = "?";

        Integer bundleId = configTask.getBundleId();
        if (bundleId != null) {
            Bundle bundle = agent.bundleContext.getBundle(bundleId);
            if (bundle != null) location = bundle.getLocation();
        } else {
            String loc = configTask.getLocation();
            if (loc != null && !loc.isEmpty()) location = loc;
        }
        if (configTask.getPid().isEmpty()) {
            result.setStatus(TaskStatus.REJECTED);
            result.setMessage("pid is empty");
            return result;
        }
        try {
            if (configTask.getProperties() != null && !configTask.getProperties().isEmpty()) {
                Configuration configuration = configurationAdmin.getConfiguration(configTask.getPid(), location);
                Dictionary<String, Object> props = configuration.getProperties();
                if (props == null) props = new Hashtable<>();
                for (String key : configTask.getProperties().keySet()) {
                    props.put(key, configTask.getProperties().get(key));
                }
                configuration.update(props);
            }
            Dictionary<String, Object> dictionary = configurationAdmin.getConfiguration(configTask.getPid(), location).getProperties();
            Hashtable<String, Object> hashtable = new Hashtable<>();

            if (dictionary != null) {
                Enumeration<String> enumeration = dictionary.keys();
                while (enumeration.hasMoreElements()) {
                    String key = enumeration.nextElement();
                    hashtable.put(key, dictionary.get(key));
                }
            }

            result.setProperties(hashtable);
        } catch (Exception e) {
            log.error("config task execution failed", e);
        }*/

    result.setStatus(TaskStatus.REJECTED);
    result.setMessage("Not implemented");
    return result;
  }
}
