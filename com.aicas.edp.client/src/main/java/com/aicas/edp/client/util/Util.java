/*------------------------------------------------------------------------*
 * Copyright 2021-2024, aicas GmbH; all rights reserved.
 * This header, including copyright notice, may not be altered or removed.
 *------------------------------------------------------------------------*/
package com.aicas.edp.client.util;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import lombok.extern.slf4j.Slf4j;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Util
{

  public static boolean notEmpty(String string)
  {
    return string != null && !string.isEmpty();
  }

  public static boolean isEmpty(String string)
  {
    return string == null || string.isEmpty();
  }

  public static URL fileToURL(String filename)
  {
    File file = new File(filename);
    URL url = null;

    if (file.exists() && file.isFile())
    {
      try
      {
        url = file.toURI().toURL();
      }
      catch (MalformedURLException ex)
      {
        log.error("Failed to convert file {} to URL", file, ex);
      }
    }
    return url;
  }

  public static Map<String, String> parseCertificateDN(String certificateDN)
  {
    Map<String, String> attributeMap = new HashMap<>();
    try
    {
      LdapName ldapDN = new LdapName(certificateDN);
      for (Rdn rdn : ldapDN.getRdns())
      {
        attributeMap.put(rdn.getType().toUpperCase(), (String) rdn.getValue());
      }
    }
    catch (Exception e)
    {
      log.error("Failed to parse Certificate DN");
    }
    return attributeMap;
  }

  public static KeyStore readKeyStore(URL keystoreURL, String keystorePwd)
  {
    try
    {
      log.trace("Reading keystore");
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(keystoreURL.openStream(), keystorePwd.toCharArray());
      return ks;
    }
    catch (KeyStoreException | IOException | CertificateException |
           NoSuchAlgorithmException e)
    {
      throw new RuntimeException("Failed to read Keystore", e);
    }
  }

  public static CertInfo parseCertInfo(KeyStore keyStore)
  {
    try
    {
      String CERTIFICATE_ALIAS = "1";
      X509Certificate certificate =
        (X509Certificate) keyStore.getCertificate(CERTIFICATE_ALIAS);
      Map<String, String> parsedDN =
        parseCertificateDN(certificate.getSubjectDN().getName());
      return CertInfo.builder()
        .thingName(parsedDN.get("CN"))
        .organization(parsedDN.get("O"))
        .environment(parsedDN.get("OU"))
        .deviceType(parsedDN.get("ST"))
        .build();
    }
    catch (KeyStoreException e)
    {
      log.error("failed to get certificate from Keystore {}", keyStore, e);
      throw new RuntimeException(e);
    }
  }

  public static AWSIotMessage createWillMessage(String thingId)
  {
    String topic = "things/" + thingId + "/update";
    AWSIotMessage message = new AWSIotMessage(topic, AWSIotQos.QOS0);
    message.setStringPayload("{\"state\":{\"reported\":{\"online\":false}}}");
    return message;
  }
}
