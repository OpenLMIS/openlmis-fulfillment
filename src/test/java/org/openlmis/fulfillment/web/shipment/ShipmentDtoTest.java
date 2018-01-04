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

package org.openlmis.fulfillment.web.shipment;

import be.joengenduvel.java.verifiers.ToStringVerifier;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;

public class ShipmentDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(ShipmentDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .withIgnoredFields("serviceUrl")
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ShipmentDto shipmentDto = new ShipmentDto();
    shipmentDto.setServiceUrl("localhost");
    new ShipmentDataBuilder().build().export(shipmentDto);
    ToStringVerifier
        .forClass(ShipmentDto.class)
        .ignore("$jacocoData") // external library is checking for this field, has to be ignored
        .ignore("lineItems") // library doesn't support lists
        .containsAllPrivateFields(shipmentDto);
  }
}