/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import com.aicas.edp.client.common.VMAccelerationInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * Application configuration for virtual machine
 */
@Slf4j
public class JamaicaVmInfo
{
  private static final String MODULE_CLASS_NAME =
    "com.aicas.jamaica.lang.ModuleLoader";
  private static final String ABI_VERSION_METHOD_NAME = "getJaraABINumber";
  private static final String VM_INTERFACE_CLASS_NAME =
    "com.aicas.jamaica.lang.VMInterface";
  private static final String TARGET_METHOD_NAME = "getTarget";
  private static final String IS_MULTICORE_METHOD_NAME = "isMulticore";
  private static final String MOCK_TARGET_ENVIRONMENT_VARIABLE = "MOCK_TARGET";
  private static final String MOCK_MULTICORE_ENVIRONMENT_VARIABLE =
    "MOCK_MULTICORE";
  private static final String MOCK_ABI_ENVIRONMENT_VARIABLE = "MOCK_ABI";
  private static VMAccelerationInfo vmAccelerationInfo;

  private JamaicaVmInfo()
  {
  }


  public static VMAccelerationInfo getVmAccelerationInfo()
  {
    return vmAccelerationInfo;
  }

  public static String getVmAccelerationInfoString()
  {
    if (Objects.isNull(vmAccelerationInfo))
      return null;
    return vmAccelerationInfo.toString();
  }

  static
  {
    initialize();
  }

  private static Object callReflection(String className, String methodName)
  {
    return callReflection(className, methodName, null);
  }

  private static Object callReflection(String className, String methodName,
                                       String params)
  {
    try
    {
      Class<?> clazz = Class.forName(className);
      Method method =
        Objects.isNull(params) ? clazz.getDeclaredMethod(methodName) :
          clazz.getDeclaredMethod(methodName, params.getClass());
      if (Modifier.isPrivate(method.getModifiers()) || Modifier.isProtected(
        method.getModifiers()))
        method.setAccessible(
          true); //TODO: jamaicaVm should provide public method or system properties
      return Objects.isNull(params) ? method.invoke(null) :
        method.invoke(null, params);
    }
    catch (ClassNotFoundException e)
    {
      log.warn("cannot find class {}", className);
    }
    catch (NoSuchMethodException e)
    {
      log.warn("cannot find method {} in class {}", methodName, className);
    }
    catch (InvocationTargetException e)
    {
      log.warn("exception while getting ABI number {}", e.getMessage());
    }
    catch (IllegalAccessException e)
    {
      log.warn("method is inaccessible {}", e.getMessage());
    }
    catch (ClassCastException e)
    {
      log.warn("cannot cast {}", e.getMessage());
    }
    return null;
  }

  private static void initialize()
  {
    Integer abiNumber =
      (Integer) callReflection(MODULE_CLASS_NAME, ABI_VERSION_METHOD_NAME);
    if (Objects.isNull(abiNumber))
    {
      log.warn("application uses another VM (not JamaicaVM) ");
      return;
    }
    log.trace("got ABI number = {}", abiNumber);
    vmAccelerationInfo = new VMAccelerationInfo();

    if (Objects.nonNull(System.getenv(MOCK_ABI_ENVIRONMENT_VARIABLE)))
    {
      vmAccelerationInfo.jaraABINumber =
        Integer.valueOf(System.getenv("MOCK_ABI"));
      log.trace(
        MOCK_ABI_ENVIRONMENT_VARIABLE + " env variable exists and ABI number set to {}",
        vmAccelerationInfo.jaraABINumber);
    }
    else
    {
      vmAccelerationInfo.jaraABINumber = abiNumber;
      log.trace(
        MOCK_ABI_ENVIRONMENT_VARIABLE + " env variable does not exist and ABI number set to {}",
        vmAccelerationInfo.jaraABINumber);
    }

    log.info("The os target name is {} and is multicore is {}",
             System.getProperties(),
             Runtime.getRuntime().availableProcessors());
    if (Objects.nonNull(System.getenv(MOCK_MULTICORE_ENVIRONMENT_VARIABLE)))
    {
      vmAccelerationInfo.multicore =
        Boolean.valueOf(System.getenv("MOCK_MULTICORE"));
      log.trace(
        MOCK_MULTICORE_ENVIRONMENT_VARIABLE + " env variable exists and multicore set to {}",
        vmAccelerationInfo.multicore);
    }
    else
    {
      vmAccelerationInfo.multicore =
        (Boolean) callReflection(VM_INTERFACE_CLASS_NAME,
                                 IS_MULTICORE_METHOD_NAME);
      log.trace(
        MOCK_MULTICORE_ENVIRONMENT_VARIABLE + " env variable does not exist and multicore set to {}",
        vmAccelerationInfo.multicore);
    }

    if (Objects.nonNull(System.getenv(MOCK_TARGET_ENVIRONMENT_VARIABLE)))
    {
      vmAccelerationInfo.target =
        System.getenv(MOCK_TARGET_ENVIRONMENT_VARIABLE);
      log.trace(
        MOCK_TARGET_ENVIRONMENT_VARIABLE + " env variable exists and target set to {}",
        vmAccelerationInfo.target);
    }
    else
    {
      vmAccelerationInfo.target =
        (String) callReflection(VM_INTERFACE_CLASS_NAME, TARGET_METHOD_NAME);
      log.trace(
        MOCK_TARGET_ENVIRONMENT_VARIABLE + " env variable does not exist and target set to {}",
        vmAccelerationInfo.target);
    }
  }
}
