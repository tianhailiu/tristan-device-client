/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.job;

import com.aicas.edp.client.util.Context;
import com.aicas.edp.client.util.Manager;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class JobManager implements Manager
{
  static final String PREFIX = "$aws/things/";
  private static final String GET_JOB_ACCEPTED_POSTFIX = "/jobs/+/get/accepted";
  private static final String NOTIFY_NEXT_JOB_POSTFIX = "/jobs/notify-next";
  private final String GET_JOB_ACCEPTED_TOPIC;
  private final String NOTIFY_NEXT_JOB_TOPIC;
  private final Context context;
  private final JobHandler jobHandler;
  private final String thingName;
  private final Set<AWSIotTopic> subscribers = new HashSet<>();


  public JobManager(Context context)
  {
    this.context = context;
    thingName = context.getConfiguration().getThingName();
    jobHandler = new JobHandler(context);
    GET_JOB_ACCEPTED_TOPIC = PREFIX + thingName + GET_JOB_ACCEPTED_POSTFIX;
    NOTIFY_NEXT_JOB_TOPIC = PREFIX + thingName + NOTIFY_NEXT_JOB_POSTFIX;
    log.trace("Job Manager created");
  }

  @Override
  public void initialize()
  {
    subscribers.add(new JobListener(this, GET_JOB_ACCEPTED_TOPIC));
    subscribers.add(new JobListener(this, NOTIFY_NEXT_JOB_TOPIC));
    subscribers.add(new RejectedJobListener(thingName));
  }

  @Override
  public void connect()
  {
    try
    {
      for (AWSIotTopic subscriber : subscribers)
      {
        context.getIotManager().subscribe(subscriber);
      }
      context.getIotManager()
        .registerConnectionSuccessHandler(this::onConnectionSuccess);
      log.trace("Job Manager activated");
    }
    catch (Exception e)
    {
      log.error("Failed to subscribe listeners", e);
    }
  }

  @Override
  public void disconnect()
  {
    for (AWSIotTopic subscriber : subscribers)
    {
      try
      {
        context.getIotManager().unsubscribe(subscriber);
      }
      catch (AWSIotException e)
      {
        log.error("Failed to unsubscribe from topic {}", subscriber.getTopic(),
                  e);
      }
    }
    log.trace("Job Manager deactivated");
  }

  @Override
  public void shutdown()
  {
    jobHandler.shutdown();
  }

  void handleJobDescriptionAsync(String stringPayload)
  {
    jobHandler.handleJobDescriptionAsync(stringPayload);
  }

  private void onConnectionSuccess()
  {
    jobHandler.getNextJobRequest();
  }
}
