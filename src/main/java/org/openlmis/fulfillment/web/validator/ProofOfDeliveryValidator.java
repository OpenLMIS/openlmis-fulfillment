/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.fulfillment.web.validator;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ProofOfDeliveryValidator extends BaseValidator implements Validator {
  private static final String LINE_ITEMS_FIELD = "lineItems";

  @Value("${fulfillment.allowEmptyShipment}")
  private boolean allowEmptyShipment;

  @Override
  public boolean supports(Class<?> clazz) {
    return ProofOfDeliveryDto.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    final ProofOfDeliveryDto proofOfDeliveryDto = (ProofOfDeliveryDto) target;

    if (!allowEmptyShipment && isEmpty(proofOfDeliveryDto.getLineItems())) {
      errors.rejectValue(LINE_ITEMS_FIELD, MessageKeys.PROOF_OF_DELIVERY_LINE_ITEMS_REQUIRED);
    }
  }
}
