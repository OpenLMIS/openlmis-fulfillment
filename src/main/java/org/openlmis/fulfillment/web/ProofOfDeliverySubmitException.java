package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.service.FulfillmentException;

public class ProofOfDeliverySubmitException extends FulfillmentException {

  public ProofOfDeliverySubmitException(String messageKey) {
    super(messageKey);
  }

}
