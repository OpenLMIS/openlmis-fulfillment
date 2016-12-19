package org.openlmis.fulfillment.service;

public abstract class ConfigurationSettingException extends FulfillmentException {

  ConfigurationSettingException(String messageKey, String... params) {
    super(messageKey, params);
  }

}
