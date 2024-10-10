/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.task;

import com.aicas.edp.client.common.task.BasicTaskResult;
import com.aicas.edp.client.common.task.BundleTask;
import com.aicas.edp.client.common.task.NewDeployTask;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.task.handler.BundleTaskHandler;
import com.aicas.edp.client.task.handler.DeployTaskHandler;
import com.aicas.edp.client.task.handler.TaskHandler;
import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.Manager;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class TaskManager implements Manager
{
  private final Map<Class<? extends Task>, TaskHandler> handlerMap =
    new HashMap<>();
  private final Context context;
  private final ExecutorService executor =
    Executors.newSingleThreadScheduledExecutor();
  private AWSIotTopic taskListener;

  public TaskManager(Context context)
  {
    this.context = context;
    log.trace("Task Manager created");
  }

  @Override
  public void initialize()
  {
    handlerMap.put(BundleTask.class, new BundleTaskHandler(context));
    handlerMap.put(NewDeployTask.class, new DeployTaskHandler(context));
    //handlerMap.put(ConfigTask.class, new ConfigTaskHandler(bndCtx, ctx));
  }

  @Override
  public void connect()
    throws
    AWSIotException
  {
    taskListener = new TaskListener(context, this);
    context.getIotManager().subscribe(taskListener);
  }

  @Override
  public void disconnect()
    throws
    AWSIotException
  {
    context.getIotManager().unsubscribe(taskListener);
  }

  @Override
  public void shutdown()
  {
    executor.shutdownNow();
  }

  public boolean isValid(Task task)
  {
    TaskHandler taskHandler = handlerMap.get(task.getClass());
    if (taskHandler != null)
    {
      return handlerMap.get(task.getClass()).isValid(task);
    }
    else
    {
      return false;
    }
  }

  public CompletableFuture<TaskResult> executeAsync(Task task)
  {
    return CompletableFuture.supplyAsync(() -> execute(task), executor);
  }

  /**
   * This method runs list of {@code @Task} as 1 Task in Task executor
   * Used to execute Job tasks sequentially without interruption via Web Portal actions.
   * @param tasks List of tasks to execute sequentially
   * @return CompletableFuture from executor
   */
  public CompletableFuture<List<TaskResult>> executeAsync(List<Task> tasks)
  {
    return CompletableFuture.supplyAsync(() -> execute(tasks), executor);
  }

  private List<TaskResult> execute(List<Task> tasks)
  {
    return tasks.stream()
      .sequential()
      .map(this::execute)
      .collect(Collectors.toList());
  }

  /**
   * The main method to execute tasks, this method should always return a TaskResult
   * and throw no exceptions
   * @param task Task to execute
   * @return Result of Task execution
   */
  private TaskResult execute(Task task)
  {
    TaskHandler taskHandler = handlerMap.get(task.getClass());
    LocalDateTime started = LocalDateTime.now();

    TaskResult taskResult;
    if (taskHandler != null)
    {
      try
      {
        taskResult = handlerMap.get(task.getClass()).execute(task);
      }
      catch (Exception e)
      {
        log.error("Unexpected error during task execution. {}", task, e);
        taskResult = createTaskResult(TaskStatus.FAILED,
                                      "Unexpected error. task: " + task);
      }
    }
    else
    {
      taskResult = createTaskResult(TaskStatus.REJECTED,
                                    "Unsupported task type: " + task.getType());
    }
    taskResult.setStarted(started);
    taskResult.setFinished(LocalDateTime.now());
    return taskResult;
  }

  private TaskResult createTaskResult(TaskStatus status, String message)
  {
    TaskResult taskResult = new BasicTaskResult();
    taskResult.setStatus(status);
    taskResult.setMessage(message);
    return taskResult;
  }
}
