/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Deprecated
public class Dependency
{
  public String symbolicName;
  public String version;
  public String url;
  public Long size;
  public List<String> accelerated;
  public String hash;
}
