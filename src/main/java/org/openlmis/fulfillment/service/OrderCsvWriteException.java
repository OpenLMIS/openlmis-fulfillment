package org.openlmis.fulfillment.service;

/**
 * Signals an issue with creating an order CSV file.
 */
public class OrderCsvWriteException extends OrderFileException {

  public OrderCsvWriteException(Throwable cause, String messageKey, String... params) {
    super(cause, messageKey, params);
  }

}
