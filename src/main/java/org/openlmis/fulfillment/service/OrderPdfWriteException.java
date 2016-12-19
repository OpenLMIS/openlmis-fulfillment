package org.openlmis.fulfillment.service;

/**
 * Signals an issues with the creation of an order PDF file.
 */
public class OrderPdfWriteException extends OrderFileException {

  OrderPdfWriteException(Throwable cause, String messageKey) {
    super(cause, messageKey);
  }

}
