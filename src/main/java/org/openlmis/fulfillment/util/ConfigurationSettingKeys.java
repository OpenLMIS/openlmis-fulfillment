package org.openlmis.fulfillment.util;

public abstract class ConfigurationSettingKeys {

  public static final String FULFILLMENT_EMAIL_NOREPLY
      = "fulfillment.email.noreply";
  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT
      = "fulfillment.email.order-creation.subject";
  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_BODY
      = "fulfillment.email.order-creation.body";

  private ConfigurationSettingKeys() {
    throw new UnsupportedOperationException();
  }

}
