package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.service.FulfillmentException;

public class ValidationException extends FulfillmentException {

  ValidationException(String messageKey, String... params) {
    super(messageKey, params);
  }

}
