package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_STATUS_INVALID;

import org.openlmis.fulfillment.service.FulfillmentException;

public class InvalidOrderStatusException extends FulfillmentException {
  public InvalidOrderStatusException(String status) {
    super(ERROR_STATUS_INVALID, status);
  }
}
