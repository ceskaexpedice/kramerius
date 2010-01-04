package com.qbizm.kramerius.imptool.poc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.qbizm.kramerius.imptool.poc.valueobj.ServiceException;

/**
 * Sprava konfigurace programu - nacitani konfigurace z externiho souboru
 * 
 * @author xholcik
 */
public class ConfigurationUtils {

  private Properties properties;

  private static final ConfigurationUtils instance = new ConfigurationUtils();

  public static ConfigurationUtils getInstance() {
    return instance;
  }

  public void initialize(String configPath) throws ServiceException {
    properties = new Properties();
    try {
      properties.load(new FileInputStream(configPath));
    } catch (IOException e) {
      throw new ServiceException(e);
    }
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

}
