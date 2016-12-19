package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_CONFIGURATION_SETTING_NOT_FOUND;

public class ConfigurationSettingNotFoundException extends ConfigurationSettingException {

  ConfigurationSettingNotFoundException(String key) {
    super(ERROR_CONFIGURATION_SETTING_NOT_FOUND, key);
  }

}
