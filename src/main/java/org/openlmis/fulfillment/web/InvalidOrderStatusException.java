package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_STATUS_INVALID;

import org.openlmis.fulfillment.service.FulfillmentException;

/**
 * Created by user on 20.12.16.
 */
public class InvalidOrderStatusException extends FulfillmentException {
  public InvalidOrderStatusException(String message) {
    super(ERROR_STATUS_INVALID, message);
  }
}
