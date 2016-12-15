package org.openlmis.fulfillment.service;

public class ConfigurationSettingNotFoundException extends ConfigurationSettingException {

  public ConfigurationSettingNotFoundException(String key) {
    super("Configuration setting '" + key + "' not found");
  }

}
