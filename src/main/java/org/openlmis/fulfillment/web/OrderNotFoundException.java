package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_NOT_FOUND;

import org.openlmis.fulfillment.service.FulfillmentException;

import java.util.UUID;

public class OrderNotFoundException extends FulfillmentException {

  public OrderNotFoundException(UUID id) {
    super(ERROR_ORDER_NOT_FOUND, id.toString());
  }

}
