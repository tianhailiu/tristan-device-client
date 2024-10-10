/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.common.task;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Deprecated
public class DeploymentPlan
{
  public String name;
  public List<String> accelerated = new ArrayList<>();
  public List<Dependency> requiredDependencies = new ArrayList<>();
  public List<Dependency> optionalDependencies = new ArrayList<>();
}
