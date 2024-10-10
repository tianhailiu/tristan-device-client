/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.shadow;

import com.aicas.edp.client.common.bundle.BundleInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class BundleInfoSerializer extends StdSerializer<BundleInfo>
{

  BundleInfoSerializer(Class<BundleInfo> t)
  {
    super(t);
  }

  @Override
  public void serialize(BundleInfo value, JsonGenerator gen,
                        SerializerProvider provider)
    throws
    IOException
  {
    gen.writeString(
      value.id + ":" + value.state.code() + ":" + value.symbolicName + ":" + value.version);
  }
}
