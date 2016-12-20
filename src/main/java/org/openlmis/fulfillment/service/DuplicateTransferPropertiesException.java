package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_TRANSFER_PROPERTIES_DUPLICATE;

public class DuplicateTransferPropertiesException extends FulfillmentException {

  DuplicateTransferPropertiesException() {
    super(ERROR_TRANSFER_PROPERTIES_DUPLICATE);
  }

}
