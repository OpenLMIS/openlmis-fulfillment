package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_TRANSFER_PROPERTIES_INCORRECT;

public class IncorrectTransferPropertiesException extends FulfillmentException {

  public IncorrectTransferPropertiesException() {
    super(ERROR_TRANSFER_PROPERTIES_INCORRECT);
  }

}
