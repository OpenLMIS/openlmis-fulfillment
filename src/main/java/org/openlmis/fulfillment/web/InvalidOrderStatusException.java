package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.service.FulfillmentException;

public class InvalidOrderStatusException extends FulfillmentException {

  InvalidOrderStatusException(String messageKey, OrderStatus status) {
    super(messageKey, status.toString());
  }

}
