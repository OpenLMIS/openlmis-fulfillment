package org.openlmis.fulfillment.service;

/**
 * Signals an issue with creating an order CSV file.
 */
public class OrderCsvWriteException extends OrderFileException {

  public OrderCsvWriteException(String message, Throwable cause) {
    super(message, cause);
  }
}
