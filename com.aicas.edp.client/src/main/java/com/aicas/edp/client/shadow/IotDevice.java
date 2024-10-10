/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.shadow;

import com.aicas.edp.client.common.bundle.BundleInfo;
import com.aicas.edp.client.common.shadow.Desired;
import com.aicas.edp.client.common.shadow.ReportedShadow;
import com.aicas.edp.client.common.shadow.Shadow;
import com.aicas.edp.client.util.Context;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class IotDevice extends AWSIotDevice
{
  // JEDP-1169: shadow update default value of 1000ms if the environment variable is not set
  private static final Integer updateIntervalMs = Optional.ofNullable(System.getenv("EDP_CLIENT_SHADOW_UPDATE"))
                                                          .map(Integer::parseInt)
                                                          .orElse(1000);
  private final ObjectMapper mapper;
  private final Context context;
  private final AtomicReference<Shadow> cloudShadow = new AtomicReference<>();
  private final AtomicBoolean updateScheduled = new AtomicBoolean(false);
  private final AtomicBoolean updateRequired = new AtomicBoolean(false);
  private final ScheduledExecutorService executor =
    Executors.newSingleThreadScheduledExecutor();

  public IotDevice(Context context)
  {
    super(context.getConfiguration().getThingName());
    this.context = context;
    mapper = createMapper();
    // We do not use scheduling reporting to cloud
    this.setReportInterval(0);
  }

  public void init()
    throws
    AWSIotException
  {
    Shadow shadow = new Shadow();
    cloudShadow.set(shadow);
    this.get(new GetShadowMessage(this), 60 * 1000);
  }

  public void shutdown()
  {
    executor.shutdownNow();
  }

  public void onShadowGetResult(String payload)
  {
    try
    {
      Shadow arrivedShadow = mapper.readValue(payload, Shadow.class);
      cloudShadow.set(arrivedShadow);

    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to parse get Shadow response", e);
    }
  }

  @Override
  public void onShadowUpdate(String deltaState)
  {
    log.debug("OnShadowUpdate body - {}", deltaState);

    try
    {
      Desired desired = mapper.readValue(deltaState, Desired.class);
      cloudShadow.updateAndGet(shadow -> {
        shadow.state.desired = desired;
        return shadow;
      });
      if (desired != null && desired.tasks != null && !desired.tasks.isEmpty())
      {
        log.trace("There are tasks in desired, delegating to ShadowManager");
        context.getShadowManager().handleDeltaUpdate(desired);
      }
    }
    catch (IOException e)
    {
      log.error("Failed to deserialize shadow update message", e);
    }
    catch (Throwable e)
    {
      log.error("Something went completely wrong", e);
    }
  }

  /**
   * This method is overridden to make sure,
   * reporting will not break client even if enabled accidentally.
   * @return Current state of device shadow.
   */
  @Override
  public String onDeviceReport()
  {
    try
    {
      return mapper.writeValueAsString(
        context.getShadowManager().getLocalShadow().state.reported);
    }
    catch (JsonProcessingException e)
    {
      log.error("Error on serializing", e);
    }
    return null;
  }

  public Shadow getCloudShadow()
  {
    return cloudShadow.get();
  }

  /**
   * Shadow manager will call this method whenever the local Shadow changes.
   * This method will make the actual update maximum once in X time.
   * this update throttling is achieved in following way:
   *      If there is no update scheduled, schedule it immediately.
   *      After the update, update method will schedule itself again after X time.
   *      If nothing is changed in X time,schedule lock will be released,
   *      and the next update will be scheduled whenever it comes.
   *
   *      if there are changes, this method will continue to push local shadow
   *      to the cloud once in X time.
   */
  void notifyLocalShadowUpdated()
  {
    log.debug(
      "Received local shadow updated notification. updateRequired:{}, updateScheduled:{}",
      updateRequired.get(), updateScheduled.get());
    updateRequired.set(true);
    if (updateScheduled.compareAndSet(false, true))
    {
      log.debug("Scheduling update immediately");
      executor.schedule(this::updateCloudShadow, 0, TimeUnit.SECONDS);
    }
    else
    {// else here, means update is already scheduled.
      log.debug("Update in progress, will run later");
    }
  }

  void updateImmediately()
  {
    updateCloudShadowInternal();
  }

  private void updateCloudShadow()
  {
    log.trace("running updateCloudShadow... updateRequired:{}",
              updateRequired.get());
    boolean wasTrue = updateRequired.compareAndSet(true, false);

    if (wasTrue)
    { // It is the First run, or local shadow updated between 2 runs (in X time)
      log.debug("Updating cloud Shadow");
      updateCloudShadowInternal();
      // Rescheduling 1 more time, to maintain throttling
      executor.schedule(this::updateCloudShadow, updateIntervalMs,
                        TimeUnit.MILLISECONDS);
    }
    else
    {// was false, it is the second run, no update in previous X time. Do not reschedule
      log.trace("nothing to update, going off");
      updateScheduled.set(false);
    }
  }

  private void updateCloudShadowInternal()
  {
    log.trace("updating shadow internal");
    try
    {
      ReportedShadow reported =
        new ReportedShadow(context.getShadowManager().getLocalShadow());
      log.debug("Updating shadow {}", reported);
      this.update(mapper.writeValueAsString(reported));
    }
    catch (AWSIotException e)
    {
      log.error("Failed to update device shadow", e);
    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to serialize shadow update message", e);
    }
    catch (Throwable t)
    {
      log.error("What a Terrible Failure!", t);
    }
  }

  private ObjectMapper createMapper()
  {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                           false);
    SimpleModule module = new SimpleModule();
    module.addSerializer(BundleInfo.class,
                         new BundleInfoSerializer(BundleInfo.class));
    module.addDeserializer(BundleInfo.class,
                           new BundleInfoDeserializer(BundleInfo.class));
    //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.registerModule(module);
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
}
