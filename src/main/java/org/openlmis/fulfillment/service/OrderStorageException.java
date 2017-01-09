package org.openlmis.fulfillment.service;

public class OrderStorageException extends FulfillmentException {

  public OrderStorageException(Throwable cause, String messageKey, String... params) {
    super(cause, messageKey, params);
  }

}
