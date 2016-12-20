package org.openlmis.fulfillment.service;

public class ReportingException extends FulfillmentException {

  public ReportingException(String messageKey, String... params) {
    super(messageKey, params);
  }

  public ReportingException(Throwable cause, String messageKey, String... params) {
    super(cause, messageKey, params);
  }

}
