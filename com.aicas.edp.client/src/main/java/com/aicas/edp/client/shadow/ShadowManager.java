/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.shadow;

import com.aicas.edp.client.common.bundle.BundleInfo;
import com.aicas.edp.client.common.shadow.Desired;
import com.aicas.edp.client.common.shadow.Reported;
import com.aicas.edp.client.common.shadow.Shadow;
import com.aicas.edp.client.common.task.BasicTaskResult;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.JamaicaVmInfo;
import com.aicas.edp.client.util.Manager;
import com.amazonaws.services.iot.client.AWSIotException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ShadowManager implements Manager
{
  private final Context context;
  private IotDevice iotDevice;
  private final AtomicReference<Shadow> shadowLocal = new AtomicReference<>();
  private final Map<String, CompletableFuture<TaskResult>> scheduledTasks =
    new ConcurrentHashMap<>();

  public ShadowManager(Context context)
  {
    this.context = context;
    log.trace("Shadow Manager created");
    shadowLocal.set(new Shadow());
  }

  @Override
  public void initialize()
  {
    iotDevice = new IotDevice(context);
    populateDeviceInfo();
    setOnline();
  }

  @Override
  public void connect()
    throws
    AWSIotException
  {
    context.getIotManager().attach(iotDevice);
    iotDevice.activate();
    //iotDevice.setEnableVersioning(true);
    iotDevice.init();
    log.trace("Shadow Manager activated");
  }

  @Override
  public void disconnect()
    throws
    AWSIotException
  {
    setOffline();
    iotDevice.deactivate();
    context.getIotManager().detach(iotDevice);
  }

  @Override
  public void shutdown()
  {
    iotDevice.shutdown();
  }

  /**
   * This is a non-blocking method to delegate handling of new Shadow delta update
   * @param desired Desired part of shadow (that comes in Delta updates)
   */
  public void handleDeltaUpdate(Desired desired)
  {
    log.trace("Handling Delta update");
    for (String taskId : desired.tasks.keySet())
    {
      Map<String, TaskResult> taskResults =
        shadowLocal.get().state.reported.taskResults;
      if (taskResults != null)
      {
        if (taskResults.containsKey(taskId))
        {
          log.trace("task # {} already processed", taskId);
          continue;
        }
        else if (scheduledTasks.containsKey(taskId))
        {
          log.trace("task # {} already scheduled", taskId);
          continue;
        }
      }
      log.trace("Executing task with id:{}", taskId);
      CompletableFuture<TaskResult> future = this.context.getTaskManager()
        .executeAsync(desired.tasks.get(taskId))
        .whenComplete(
          (taskResult, throwable) -> handleAsyncTaskResult(taskId, taskResult,
                                                           throwable));
      this.scheduledTasks.put(taskId, future);
    }
  }

  /**
   * Helper method, that is called after Execute Task future is completed.
   * Method handles result of the Task execution, either TaskResult or Throwable.
   *
   * @param taskId taskId of Task that produced following TaskResult
   * @param taskResult Result of the Task execution
   * @param throwable Exception if Task execution failed
   */
  private void handleAsyncTaskResult(String taskId, TaskResult taskResult,
                                     Throwable throwable)
  {
    if (taskResult == null && throwable != null)
    {
      log.error("Unexpected error during task {} execution, t", taskId,
                throwable);
      taskResult = new BasicTaskResult();
      taskResult.setTaskId(taskId);
      taskResult.setStatus(TaskStatus.FAILED);
      taskResult.setMessage("Unexpected exception: " + throwable.getMessage());
    }
    TaskResult finalTaskResult = taskResult;
    shadowLocal.updateAndGet(shadow -> {
      Map<String, TaskResult> taskResults = shadow.state.reported.taskResults;
      if (taskResults == null)
      {
        taskResults = new HashMap<>();
        shadow.state.reported.taskResults = taskResults;
      }
      taskResults.put(taskId, finalTaskResult);
      return shadow;
    });
    scheduledTasks.remove(taskId);
    houseHolding();
    iotDevice.notifyLocalShadowUpdated();
    System.out.println("");
  }

  private void houseHolding()
  {
    // First, lets remove TaskResults, whose corresponding Tasks are already removed by the Backend.
    Shadow cloudShadow = iotDevice.getCloudShadow();
    shadowLocal.getAndUpdate(shadow -> {
      shadow.state.reported.taskResults.forEach((taskId, taskResult) -> {
        // If there are no Task with same id in Desired - remove Task Result
        if (!cloudShadow.state.desired.tasks.containsKey(taskId))
        {
          // if TaskResult value is already null, then cloud should be already updated,
          // and nulls are no longer needed.
          if (shadow.state.reported.taskResults.get(taskId) == null)
          {
            shadow.state.reported.taskResults.remove(taskId);
          }
          else
          {
            shadow.state.reported.taskResults.replace(taskId, null);
          }
        }
      });
      return shadow;
    });
  }

  public Shadow getLocalShadow()
  {
    return shadowLocal.get();
  }

  public void updateBundles(List<BundleInfo> bundles)
  {
    shadowLocal.updateAndGet(shadow -> {
      shadow.state.reported.setBundles(bundles);
      return shadow;
    });
    iotDevice.notifyLocalShadowUpdated();
  }

  private void populateDeviceInfo()
  {
    shadowLocal.getAndUpdate(shadow -> {
      Reported reported = shadow.state.reported;
      reported.arch = System.getProperty("os.arch");
      reported.osName = System.getProperty("os.name");
      reported.osVersion = System.getProperty("os.version");
      reported.javaRuntimeName = System.getProperty("java.runtime.name");
      reported.javaRuntimeVersion = System.getProperty("java.runtime.version");
      reported.vmAccelerationInfo = JamaicaVmInfo.getVmAccelerationInfoString();
      return shadow;
    });
  }

  private void setOnline()
  {
    shadowLocal.getAndUpdate(shadow -> {
      shadow.state.reported.online = true;
      iotDevice.notifyLocalShadowUpdated();
      return shadow;
    });
  }

  private void setOffline()
  {
    log.trace("Setting device status offline");
    shadowLocal.getAndUpdate(shadow -> {
      shadow.state.reported.online = false;
      return shadow;
    });
    iotDevice.updateImmediately();
  }


}

