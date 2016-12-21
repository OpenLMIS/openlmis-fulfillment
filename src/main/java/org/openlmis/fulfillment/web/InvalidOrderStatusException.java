package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;

import org.openlmis.fulfillment.service.FulfillmentException;

public class InvalidOrderStatusException extends FulfillmentException {
  public InvalidOrderStatusException(String status) {
    super(ERROR_ORDER_INVALID_STATUS, status);
  }
}
