package org.openlmis.fulfillment.service;

/**
 * Signals an issue with creating the order file.
 */
public abstract class OrderFileException extends FulfillmentException {

  OrderFileException(Throwable cause, String messageKey) {
    super(cause, messageKey);
  }

}
