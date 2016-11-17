package org.openlmis.order.domain;

/**
 * Signals an issue with generating order number.
 */
public class OrderNumberException extends RuntimeException {

  public OrderNumberException(String message) {
    super(message);
  }
}
