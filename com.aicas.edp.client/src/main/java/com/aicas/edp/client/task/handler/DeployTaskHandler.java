/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.task.handler;

import com.aicas.edp.client.bundle.model.SignUrlRequest;
import com.aicas.edp.client.bundle.model.SignUrlResponse;
import com.aicas.edp.client.common.VMAccelerationInfo;
import com.aicas.edp.client.common.task.BundleTaskResult;
import com.aicas.edp.client.common.task.InstallBundle;
import com.aicas.edp.client.common.task.NewDeployTask;
import com.aicas.edp.client.common.task.Task;
import com.aicas.edp.client.common.task.TaskResult;
import com.aicas.edp.client.common.task.TaskStatus;
import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.JamaicaVmInfo;
import com.amazonaws.services.iot.client.AWSIotException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class DeployTaskHandler implements TaskHandler
{
  private final Context context;

  public DeployTaskHandler(Context context)
  {
    log.trace("BundleTaskHandler created");
    this.context = context;
  }

  @Override
  public boolean isValid(Task task)
  {
    if (!(task instanceof NewDeployTask))
    {
      log.error("Validation failed: Task {} is not instance of DeployTask",
                task.getClass());
      return false;
    }
    NewDeployTask t = (NewDeployTask) task;
    if (t.installs == null || t.installs.isEmpty())
    {
      log.error("Validation failed: Required dependency list is empty");
      return false;
    }
    for (InstallBundle d : t.installs)
    {
      if (d.symbolicName == null || d.symbolicName.isEmpty() || d.version == null || d.version.isEmpty())
      {
        log.error("Validation failed: symbolicName and/or version is empty");
        return false;
      }
    }
    return true;
  }

  @Override
  public TaskResult execute(Task task)
  {

    BundleTaskResult result = new BundleTaskResult();
    result.setTaskId(task.getId());
    if (!(task instanceof NewDeployTask))
      throw new RuntimeException(
        "DeployTaskHandler handles only DeployTask subtype, received " + task.getType());

    NewDeployTask newDeployTask = (NewDeployTask) task;

    try
    {
      for (InstallBundle installBundle : newDeployTask.installs)
      {
        installDependency(installBundle, newDeployTask);
      }
      result.setStatus(TaskStatus.SUCCEEDED);
    }
    catch (DeployTaskException e)
    {
      result.setStatus(TaskStatus.FAILED);
      result.setMessage(e.getMessage());
    }
    //installDependency(deployTask.deploymentPlan.requiredDependencies.get(0), deployTask);

    return result;
  }

  // TODO Add input stream closing
  // TODO handle update bundle if it is exists and has lower version
  private void installDependency(InstallBundle installBundle,
                                 NewDeployTask newDeployTask)
    throws
    DeployTaskException
  {
    try
    {
      VMAccelerationInfo vmAccelerationInfo =
        JamaicaVmInfo.getVmAccelerationInfo();
      SignUrlRequest signUrlRequest = new SignUrlRequest();
      signUrlRequest.symbolicName = installBundle.symbolicName;
      signUrlRequest.version = installBundle.version;
      if (vmAccelerationInfo != null)
      {
        signUrlRequest.target = vmAccelerationInfo.target;
        signUrlRequest.jaraABINumber = vmAccelerationInfo.jaraABINumber;
        signUrlRequest.multicore = vmAccelerationInfo.multicore;
      }

      SignUrlResponse response = getSignUrlResponse(signUrlRequest);
      URL url = new URL(response.signedUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      try
      {
        //con.setRequestProperty("token", token);
        int status = con.getResponseCode();
        log.info("Request status: {}", status);

        if (status == 200)
        {
          Bundle bundle1 = context.getBundleContext()
            .installBundle(response.signedUrl, con.getInputStream());
          log.info("Successfully installed {}", bundle1.getLocation());
          // printResponseContent(con);
        }
        else
        {
          printErrorResponseContent(con);
          log.error("Failed to download bundle, status code {}", status);
          throw new Exception(
            String.format("Failed to download bundle, status code %d", status));
        }
      }
      finally
      {
        con.disconnect();
      }
    }
    catch (BundleException | ExecutionException | TimeoutException |
           IOException | AWSIotException | InterruptedException e)
    {
      if (e instanceof BundleException && e.getMessage()
        .contains("Bundle symbolic name and version are not unique"))
      {
        log.info("Bundle {}:{} already installed", installBundle.symbolicName,
                 installBundle.version);
      }
      else
      {
        log.error("", e);
        throw new DeployTaskException(e.getMessage());
      }
    }
    catch (Exception e)
    {
      log.error("", e);
      throw new DeployTaskException(e.getMessage());
    }
  }

  //AWS lambda manages if the sign response is not present and give actual
  private SignUrlResponse getSignUrlResponse(SignUrlRequest signUrlRequest)
    throws
    AWSIotException,
    ExecutionException,
    InterruptedException,
    TimeoutException
  {
    CompletableFuture<SignUrlResponse> tokenFuture =
      context.getBundleManager().getUrlSigner().getSignedUrl(signUrlRequest);
    SignUrlResponse response = tokenFuture.get(60, TimeUnit.SECONDS);
    log.debug("{} {} {}", tokenFuture.isCancelled(),
              tokenFuture.isCompletedExceptionally(), tokenFuture.isDone());
    return response;
  }

  //TODO Debug method
  private void printResponseContent(HttpURLConnection con)
  {
    try (BufferedReader in = new BufferedReader(
      new InputStreamReader(con.getInputStream())))
    {
      StringBuilder content = new StringBuilder();
      in.lines().forEach(content::append);
      log.info("{}", content);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  //TODO Debug method
  private void printErrorResponseContent(HttpURLConnection con)
  {
    try (BufferedReader in = new BufferedReader(
      new InputStreamReader(con.getErrorStream())))
    {
      StringBuilder content = new StringBuilder();
      in.lines().forEach(content::append);
      log.info("{}", content);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private static class DeployTaskException extends Exception
  {
    public DeployTaskException(String message)
    {
      super("exception while deploying task " + message);
    }
  }
}
