/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstallBundle
{
  public String symbolicName;
  public String version;
}
