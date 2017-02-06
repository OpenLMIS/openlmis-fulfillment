package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.service.FulfillmentException;

public class ValidationException extends FulfillmentException {

  public ValidationException(String messageKey, String... params) {
    super(messageKey, params);
  }

}
