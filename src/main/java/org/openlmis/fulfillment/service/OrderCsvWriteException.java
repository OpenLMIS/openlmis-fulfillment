package org.openlmis.fulfillment.service;

/**
 * Signals an issue with creating an order CSV file.
 */
public class OrderCsvWriteException extends OrderFileException {

  OrderCsvWriteException(Throwable cause, String messageKey) {
    super(cause, messageKey);
  }

}
