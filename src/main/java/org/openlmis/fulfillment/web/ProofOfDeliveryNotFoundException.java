package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PROOF_OF_DELIVERY_NOT_FOUND;

import org.openlmis.fulfillment.service.FulfillmentException;

import java.util.UUID;

public class ProofOfDeliveryNotFoundException extends FulfillmentException {

  public ProofOfDeliveryNotFoundException(UUID id) {
    super(ERROR_PROOF_OF_DELIVERY_NOT_FOUND, id.toString());
  }

}
