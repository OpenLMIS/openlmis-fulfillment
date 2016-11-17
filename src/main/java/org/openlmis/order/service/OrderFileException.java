package org.openlmis.order.service;

/**
 * Signals an issue with creating the order file.
 */
public abstract class OrderFileException extends Exception {

  public OrderFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
