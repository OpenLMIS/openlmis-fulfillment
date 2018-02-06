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

package org.openlmis.fulfillment.domain;

import static org.hamcrest.Matchers.startsWith;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_INCORRECT_QUANTITIES;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_INCORRECT_VVM_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_MISSING_REASON;
import static org.openlmis.fulfillment.i18n.MessageKeys.MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.fulfillment.ProofOfDeliveryLineItemDataBuilder;
import org.openlmis.fulfillment.domain.naming.VvmStatus;
import org.openlmis.fulfillment.web.ValidationException;

public class ProofOfDeliveryLineItemTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldThrowExceptionIfQuantityAcceptedIsLessThanZero() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO));
    new ProofOfDeliveryLineItemDataBuilder().withIncorrectQuantityAccepted().build().validate(null);
  }

  @Test
  public void shouldThrowExceptionIfQuantityRejectedIsLessThanZero() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO));
    new ProofOfDeliveryLineItemDataBuilder().withIncorrectQuantityRejected().build().validate(null);
  }

  @Test
  public void shouldThrowExceptionIfUseVvmAndStatusIsNull() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(ERROR_INCORRECT_VVM_STATUS));
    new ProofOfDeliveryLineItemDataBuilder().withoutVvmStatus().build().validate(null);
  }

  @Test
  public void shouldThrowExceptionIfUseVvmAndStatusIsIncorrect() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(ERROR_INCORRECT_VVM_STATUS));
    new ProofOfDeliveryLineItemDataBuilder()
        .withVvmStatus(VvmStatus.STAGE_4)
        .build()
        .validate(null);
  }

  @Test
  public void shouldThrowExceptionIfReasonIsNotProvided() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(ERROR_MISSING_REASON));
    new ProofOfDeliveryLineItemDataBuilder().withoutReason().build().validate(null);
  }

  @Test
  public void shouldThrowExceptionIfSumIsIncorrect() {
    exception.expect(ValidationException.class);
    exception.expectMessage(startsWith(ERROR_INCORRECT_QUANTITIES));

    ProofOfDeliveryLineItem line = new ProofOfDeliveryLineItemDataBuilder().build();
    // we calculate shipped quantity in this way to make sure that the sum of accepted and
    // rejected quantities will be incorrect
    Long quantityShipped = (long) (line.getQuantityAccepted() + line.getQuantityRejected() + 10);
    line.validate(quantityShipped);
  }

  @Test
  public void shouldValidate() {
    ProofOfDeliveryLineItem line = new ProofOfDeliveryLineItemDataBuilder().build();
    Long quantityShipped = (long) (line.getQuantityAccepted() + line.getQuantityRejected());
    line.validate(quantityShipped);
  }

}
