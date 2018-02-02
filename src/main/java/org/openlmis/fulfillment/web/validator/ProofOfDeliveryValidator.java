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

import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;

import com.google.common.collect.Lists;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.util.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProofOfDeliveryValidator extends BaseValidator {

  /**
   * Valides the given proof of delivery.
   *
   * @param target instance of {@link ProofOfDelivery} that should be validated.
   */
  public List<Message.LocalizedMessage> validate(ProofOfDelivery target) {
    List<Message.LocalizedMessage> errors = Lists.newArrayList();

    rejectIfBlank(errors, target.getDeliveredBy(), DELIVERED_BY);
    rejectIfBlank(errors, target.getReceivedBy(), RECEIVED_BY);
    rejectIfNull(errors, target.getReceivedDate(), RECEIVED_DATE);

    return errors;
  }

}
