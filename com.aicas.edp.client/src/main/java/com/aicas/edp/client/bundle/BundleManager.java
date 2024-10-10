/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.bundle;

import com.aicas.edp.client.bundle.model.SignUrlRequest;
import com.aicas.edp.client.bundle.model.SignUrlResponse;
import com.aicas.edp.client.common.VMAccelerationInfo;
import com.aicas.edp.client.common.bundle.BundleEventEnum;
import com.aicas.edp.client.common.bundle.BundleInfo;
import com.aicas.edp.client.common.bundle.BundleState;
import com.aicas.edp.client.common.task.InstallBundle;
import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.JamaicaVmInfo;
import com.aicas.edp.client.util.Manager;
import com.amazonaws.services.iot.client.AWSIotException;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class BundleManager implements Manager
{
  private final Context context;
  private UrlSigner urlSigner;
  private final List<BundleInfo> bundles = new CopyOnWriteArrayList<>();

  public BundleManager(Context context)
  {
    this.context = context;
    log.trace("Bundle Manager created");
  }

  @Override
  public void initialize()
  {
    initBundleList();
    context.getBundleContext().addBundleListener(this::handleLifecycleEvent);
  }

  @Override
  public void connect()
    throws
    AWSIotException
  {
    log.trace("Activating Bundle Manager");
    urlSigner = new UrlSigner(context.getIotManager(),
                              context.getConfiguration()
                                .getCommonTopicPrefix());
    context.getIotManager().subscribe(urlSigner);

  }

  @Override
  public void disconnect()
    throws
    AWSIotException
  {
    context.getIotManager().unsubscribe(urlSigner);
    urlSigner = null;
    log.trace("Bundle Manager disconnected");
  }

  @Override
  public void shutdown()
  {
    context.getBundleContext().removeBundleListener(this::handleLifecycleEvent);
  }

  public void installBundle(String symbolicName, String version)
  {
    log.info("got symbolic name {} and version {} ", symbolicName, version);
    InstallBundle installBundle = new InstallBundle();
    installBundle.symbolicName = symbolicName;
    installBundle.version = version;
    installDep(installBundle);
  }

  public UrlSigner getUrlSigner()
  {
    return urlSigner;
  }

  private void installDep(InstallBundle installBundle)
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
      CompletableFuture<SignUrlResponse> tokenFuture =
        urlSigner.getSignedUrl(signUrlRequest);
      SignUrlResponse response = tokenFuture.get(60, TimeUnit.SECONDS);
      log.debug("{} {} {}", tokenFuture.isCancelled(),
                tokenFuture.isCompletedExceptionally(), tokenFuture.isDone());
      URL url = new URL(response.signedUrl);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      //con.setRequestProperty("token", token);
      int status = con.getResponseCode();
      log.info("Request status: {}", status);

      if (status == 200)
      {
        try (InputStream is = con.getInputStream())
        {
          Bundle bundle1 =
            context.getBundleContext().installBundle(response.signedUrl, is);
          log.info("Successfully installed {}", bundle1.getLocation());
        }
      }
      else
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
        log.error("Failed to download bundle, status code {}", status);
      }
      con.disconnect();
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
        log.error("", e);
    }
    catch (Exception e)
    {
      log.error("", e);
    }
  }

  private void initBundleList()
  {
    log.debug("AppManager: scanning existing bundles...");
    List<BundleInfo> installedBundles = new ArrayList<>();
    for (Bundle bundle : context.getBundleContext().getBundles())
    {
      BundleInfo bundleInfo = new BundleInfo();
      bundleInfo.id = bundle.getBundleId();
      bundleInfo.symbolicName = bundle.getSymbolicName();
      bundleInfo.version = bundle.getVersion().toString();
      bundleInfo.state = BundleState.get(bundle.getState());
      installedBundles.add(bundleInfo);
    }
    log.debug("Found " + installedBundles.size() + " bundles");
    bundles.addAll(installedBundles);
    context.getShadowManager().updateBundles(bundles);
  }

  @SuppressWarnings("DuplicateBranchesInSwitch")
  private void handleLifecycleEvent(BundleEvent event)
  {
    log.debug("AppManager, handling lifeCycle event " + event);
    BundleEventEnum eventEnum = BundleEventEnum.get(event.getType());
    Bundle bundle = event.getBundle();
    switch (eventEnum)
    {
    case UNINSTALLED:
      removeBundleInfo(bundle);
      break;
    case INSTALLED:
      addBundleInfo(bundle);
      break;
    case RESOLVED:
      updateBundleInfo(eventEnum, bundle);
      break;
    case STARTED:
      updateBundleInfo(eventEnum, bundle);
      break;
    case STOPPED:
      updateBundleInfo(eventEnum, bundle);
      break;
    case UPDATED:
      updateBundleInfo(eventEnum, bundle);
      break;
    case UNRESOLVED:
      updateBundleInfo(eventEnum, bundle);
      break;
    case STARTING:
      updateBundleInfo(eventEnum, bundle);
      break;
    case STOPPING:
      updateBundleInfo(eventEnum, bundle);
      break;
    case LAZY_ACTIVATION:
      updateBundleInfo(eventEnum, bundle);
      break;
    default:
      log.warn("Unsupported bundle lifecycle event type: {}", event.getType());
    }
    context.getShadowManager().updateBundles(bundles);
  }

  private void addBundleInfo(Bundle bundle)
  {
    log.debug("Adding bundle {}", bundle);
    BundleInfo bundleInfo = new BundleInfo();
    bundleInfo.id = bundle.getBundleId();
    bundleInfo.symbolicName = bundle.getSymbolicName();
    bundleInfo.version = bundle.getVersion().toString();
    bundleInfo.state = BundleState.get(bundle.getState());
    bundles.add(bundleInfo);
  }

  private void updateBundleInfo(BundleEventEnum eventEnum, Bundle bundle)
  {
    log.debug("Updating bundle state to {}. bundle: {}", eventEnum, bundle);
    BundleInfo bundleInfo = null;
    for (BundleInfo item : bundles)
    {
      if (item.id.equals(bundle.getBundleId()))
      {
        item.state = BundleState.get(bundle.getState());
      }
      bundleInfo = item;
    }
    if (bundleInfo == null)
      log.error("Cannot update BundleInfo for {} = {} .not found", bundle,
                eventEnum);
  }

  private void removeBundleInfo(Bundle bundle)
  {
    log.debug("Removing bundle {}", bundle);
    boolean removed = false;
    for (BundleInfo item : bundles)
    {
      if (item.id.equals(bundle.getBundleId()))
        removed = bundles.remove(item);
    }
    if (!removed)
      log.error("Cannot remove BundleInfo for {} .not found", bundle);
  }
}
