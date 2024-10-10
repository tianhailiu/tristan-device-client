/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.shadow;

/**
 * This class is used in Backend, to update device shadow desired part only.
 * We need to serialize null, because this way AWS recognises that you want to remove
 * field from the shadow. But, if we serialize nulls, and unpopulated fields of the class
 * will be removed from the Shadow.
 * By having separate class for serialising shadow.state.desired update, we can have both:
 * null values to delete fields, and absent irrelevant fields that would be serialized as null
 * and therefore deleted from shadow by AWS. For ex. reported state can be removed
 */
public class DesiredShadow
{
  public DesiredState state = new DesiredState();
  public Long version;

  public DesiredShadow()
  {
  }

  public DesiredShadow(Shadow shadow)
  {
    state = new DesiredState(shadow.state);
    version = shadow.version;
  }
}
