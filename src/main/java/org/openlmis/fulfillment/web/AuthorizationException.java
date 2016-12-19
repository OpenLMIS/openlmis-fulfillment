package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.service.FulfillmentException;

/**
 * Signals user lacking permission to access the resource.
 */
public abstract class AuthorizationException extends FulfillmentException {

  AuthorizationException(String messageKey, String... params) {
    super(messageKey, params);
  }

}
