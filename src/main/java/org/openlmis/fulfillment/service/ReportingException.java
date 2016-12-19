package org.openlmis.fulfillment.service;

public class ReportingException extends FulfillmentException {

  public ReportingException(String messageKey) {
    super(messageKey);
  }

  public ReportingException(String messageKey, String... params) {
    super(messageKey, params);
  }

  public ReportingException(Throwable cause, String messageKey) {
    super(cause, messageKey);
  }

}
