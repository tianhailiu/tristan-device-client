/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.task.handler;

import com.aicas.edp.client.common.bundle.BundleAction;
import com.aicas.edp.client.common.bundle.BundleState;
import com.aicas.edp.client.common.task.BundleTask;
import com.aicas.edp.client.common.task.BundleTaskResult;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.util.Context;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.util.ArrayList;
import java.util.List;

import static com.aicas.edp.client.common.bundle.BundleState.ACTIVE;
import static com.aicas.edp.client.common.bundle.BundleState.INSTALLED;
import static com.aicas.edp.client.common.bundle.BundleState.RESOLVED;

@Slf4j
public class BundleTaskHandler implements TaskHandler
{
  private final Context context;

  public BundleTaskHandler(Context context)
  {
    this.context = context;
    log.trace("BundleTaskHandler created");
  }

  @Override
  public boolean isValid(Task task)
  {
    if (!(task instanceof BundleTask))
    {
      log.error("Validation failed: Task {} is not instance of BundleTask",
                task.getClass());
      return false;
    }
    BundleTask t = (BundleTask) task;
    if (t.getAction() == null)
    {
      log.error("Validation failed: BundleAction is null");
      return false;
    }
    if (t.getSymbolicName() != null && t.getSymbolicName().length() <= 1)
    {
      log.error("Validation failed: symbolicName is empty '{}'",
                t.getSymbolicName());
      return false;
    }
    return true;
  }

  @Override
  // TODO Check for Bundle version also
  public TaskResult execute(Task task)
  {
    BundleTaskResult result = new BundleTaskResult();
    result.setTaskId(task.getId());

    if (!(task instanceof BundleTask))
      throw new RuntimeException(
        "BundleTaskHandler handles only BundleTask subtype, received " + task.getType());

    BundleTask bundleTask = (BundleTask) task;
    Bundle[] bundles = context.getBundleContext().getBundles();
    Bundle target = null;
    for (Bundle bundle : bundles)
    {
      if (bundleTask.getSymbolicName().equals(bundle.getSymbolicName())
      && bundleTask.getVersion().equals(bundle.getVersion().toString()))
      {
        target = bundle;
        break;
      }
    }

    if (bundleTask.getAction() != BundleAction.LIST && target == null)
    {
      result.setMessage("Bundle not found");
      result.setStatus(TaskStatus.REJECTED);
      return result;
    }
    else if (target != null)
    {
      try
      {
        processCommand(bundleTask, target, result);
      }
      catch (Exception e)
      {
        log.error("Failed to process command", e);
        result.setMessage("Bundle not found");
        result.setStatus(TaskStatus.FAILED);
      }
    }
    else
    {
      result.setMessage("Bundle not found");
      result.setStatus(TaskStatus.REJECTED);
    }
    return result;
  }

  private void processCommand(BundleTask bundleTask, Bundle target,
                              BundleTaskResult result)
  {
    BundleState state;
    try
    {
      switch (bundleTask.getAction())
      {
      case START:
        state = BundleState.get(target.getState());
        if (state.equals(RESOLVED) || state.equals(INSTALLED))
        {
          target.start();
          state = BundleState.get(target.getState());
          if (state.equals(ACTIVE))
            result.setStatus(TaskStatus.SUCCEEDED);
          else
          {
            result.setStatus(TaskStatus.FAILED);
            result.setMessage(
              "Start bundle " + bundleTask.getSymbolicName() + " failed. " +
                "Current status " + BundleState.get(target.getState()));
          }
        }
        else
        { // State is other than RESOLVED
          result.setStatus(TaskStatus.REJECTED);
          result.setMessage(
            "Bundle " + bundleTask.getSymbolicName() + " is in state " + state);
        }
        break;

      case STOP:
        state = BundleState.get(target.getState());
        if (state.equals(ACTIVE))
        {
          target.stop();
          state = BundleState.get(target.getState());
          if (state.equals(RESOLVED))
            result.setStatus(TaskStatus.SUCCEEDED);
          else
          {
            result.setStatus(TaskStatus.FAILED);
            result.setMessage(
              "Start bundle " + bundleTask.getSymbolicName() + " failed. " +
                "Current status " + BundleState.get(target.getState()));
          }
        }
        else if (state.equals(RESOLVED))
        { // bundle already stopped.
          result.setStatus(TaskStatus.SUCCEEDED);
        }
        else
        { // bundle is not in ACTIVE state
          result.setStatus(TaskStatus.REJECTED);
          result.setMessage(
            "Bundle " + bundleTask.getSymbolicName() + " is not in ACTIVE state.");
        }
        break;
      case UNINSTALL:
        target.uninstall();
        result.setStatus(TaskStatus.SUCCEEDED);
        break;
      case LIST:
        Bundle[] bundlesArray = context.getBundleContext().getBundles();
        List<String> bundleList = new ArrayList<>();
        for (Bundle bundle : bundlesArray)
        {
          // filter uninstalled bundles from shadow bundle list.
          // currently we don't have request for list of bundles after uninstall task
          // bundle list is updated upon any bundle lifecycle event,
          // and there is no event when UNINSTALLED bundle disappears from bundle list,
          // therefore uninstalled bundle is shown in DCS until any other bundle event happens.
          // The easy solution is to not show uninstalled bundles in shadow at all.
          if (bundle.getState() != 1)
            bundleList.add(bundle.getSymbolicName());
        }
        result.setBundles(bundleList);
        result.setStatus(TaskStatus.SUCCEEDED);
        break;
      default:
        result.setStatus(TaskStatus.REJECTED);
        result.setMessage(
          "Unsupported bundle action " + bundleTask.getAction());
      }
    }
    catch (BundleException be)
    {
      log.error("Failed to execute action {}", bundleTask.getAction(), be);
      result.setStatus(TaskStatus.FAILED);
      result.setMessage(be.getMessage());
    }
  }


}
