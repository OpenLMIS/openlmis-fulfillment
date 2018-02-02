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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.fulfillment.ProofOfDeliveryDataBuilder;
import org.openlmis.fulfillment.web.ValidationException;

public class ProofOfDeliveryTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void shouldNotConfirmIfDeliveredByIsBlank() {
    setException("deliveredBy");
    new ProofOfDeliveryDataBuilder().withoutDeliveredBy().build().confirm();
  }

  @Test
  public void shouldNotConfirmIfReceivedByIsBlank() {
    setException("receivedBy");
    new ProofOfDeliveryDataBuilder().withoutReceivedBy().build().confirm();
  }

  @Test
  public void shouldNotConfirmIfReceivedDateIsNull() {
    setException("receivedDate");
    new ProofOfDeliveryDataBuilder().withoutReceivedDate().build().confirm();
  }

  @Test
  public void shouldConfirm() {
    ProofOfDelivery pod = new ProofOfDeliveryDataBuilder().build();
    pod.confirm();

    assertThat(pod.isConfirmed(), is(true));
  }

  private void setException(String field) {
    exception.expect(allOf(
        instanceOf(ValidationException.class),
        hasProperty("messageKey", is(VALIDATION_ERROR_MUST_CONTAIN_VALUE)),
        hasProperty("params", hasItemInArray(field))
    ));
  }
}
