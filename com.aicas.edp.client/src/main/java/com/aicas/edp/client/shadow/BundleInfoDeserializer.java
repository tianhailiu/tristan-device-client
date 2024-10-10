/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.shadow;

import com.aicas.edp.client.common.bundle.BundleInfo;
import com.aicas.edp.client.common.bundle.BundleState;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class BundleInfoDeserializer extends StdDeserializer<BundleInfo>
{

  protected BundleInfoDeserializer()
  {
    super(BundleInfo.class);
  }

  protected BundleInfoDeserializer(Class<?> vc)
  {
    super(vc);
  }

  @Override
  public BundleInfo deserialize(JsonParser p, DeserializationContext context)
    throws
    IOException
  {
    JsonNode node = p.getCodec().readTree(p);
    String[] bundleInfoParts = node.asText().split(":");

    if (bundleInfoParts.length <= 0)
      return null;

    BundleInfo bundleInfo = new BundleInfo();
    bundleInfo.id = Long.valueOf(bundleInfoParts[0]);
    bundleInfo.state = BundleState.get(Integer.valueOf(bundleInfoParts[1]));
    bundleInfo.symbolicName = bundleInfoParts[2];
    bundleInfo.version = bundleInfoParts[3];
    return bundleInfo;
  }
}
