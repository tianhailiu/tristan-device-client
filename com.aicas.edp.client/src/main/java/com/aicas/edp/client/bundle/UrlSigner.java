/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.bundle;

import com.aicas.edp.client.bundle.model.SignUrlRequest;
import com.aicas.edp.client.bundle.model.SignUrlResponse;
import com.aicas.edp.client.iot.IotManager;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class UrlSigner extends AWSIotTopic
{
  private static final String PRE_SIGN_RESPONSE_POSTFIX =
    "/pre-signed-url/response";
  private static final String PRE_SIGN_REQUEST_POSTFIX =
    "/pre-signed-url/request";
  private final String PRE_SIGN_REQUEST_TOPIC;
  private final IotManager iotManager;
  private final ObjectMapper mapper = new ObjectMapper();
  private CompletableFuture<SignUrlResponse> future = new CompletableFuture<>();

  public UrlSigner(IotManager iotManager, String prefix)
  {
    super(prefix + PRE_SIGN_RESPONSE_POSTFIX);
    this.iotManager = iotManager;
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    PRE_SIGN_REQUEST_TOPIC = prefix + PRE_SIGN_REQUEST_POSTFIX;
  }

  @Override
  public void onMessage(AWSIotMessage message)
  {
    log.debug("Pre-signed url response received {}",
              message.getStringPayload());
    try
    {
      SignUrlResponse response =
        mapper.readValue(message.getStringPayload(), SignUrlResponse.class);
      future.complete(response);

    }
    catch (IOException e)
    {
      future.completeExceptionally(e);
      log.error("Failed to parse pre-signed url response", e);
    }
  }

  public CompletableFuture<SignUrlResponse> getSignedUrl(SignUrlRequest request)
    throws
    AWSIotException
  {
    log.debug("Pre-signed url request received {}", request);
    future = new CompletableFuture<>();
    requestPreSignedUrl(request);
    return future;
  }

  private void requestPreSignedUrl(SignUrlRequest request)
    throws
    AWSIotException
  {
    try
    {
      String content = mapper.writeValueAsString(request);
      AWSIotMessage message =
        new AWSIotMessage(PRE_SIGN_REQUEST_TOPIC, AWSIotQos.QOS0, content);
      iotManager.publish(message);

    }
    catch (JsonProcessingException e)
    {
      log.error("Failed to serialize SignUrlRequest: {}", request, e);
    }
  }
}
