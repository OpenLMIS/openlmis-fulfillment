package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.service.FulfillmentException;

import java.util.List;

public class ValidationException extends FulfillmentException {
  private final ValidationErrors details;

  ValidationException(String messageKey, ValidationErrors details) {
    super(messageKey);
    this.details = details;
  }

  public final List<FieldError> getDetails() {
    return details.getErrors();
  }
  
}
