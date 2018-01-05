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

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.testutils.ToStringTestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShipmentLineItemTest {

  private UUID lineItemId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private Long quantityShipped = 15L;

  @Test
  public void shouldExportValues() {
    Map<String, Object> values = new HashMap<>();
    ShipmentLineItem.Exporter exporter = new ShipmentLineItem.Exporter() {
      @Override
      public void setId(UUID id) {
        values.put("id", id);
      }

      @Override
      public void setOrderableId(UUID orderableId) {
        values.put("orderableId", orderableId);
      }

      @Override
      public void setLotId(UUID lotId) {
        values.put("lotId", lotId);
      }

      @Override
      public void setQuantityShipped(Long quantityShipped) {
        values.put("quantityShipped", quantityShipped);
      }

    };

    ShipmentLineItem shipmentLineItem = createShipmentLineItem();
    shipmentLineItem.export(exporter);

    assertThat(values, hasEntry("id", lineItemId));
    assertThat(values, hasEntry("orderableId", orderableId));
    assertThat(values, hasEntry("lotId", lotId));
    assertThat(values, hasEntry("quantityShipped", quantityShipped));
  }

  private ShipmentLineItem createShipmentLineItem() {
    return new ShipmentLineItemDataBuilder()
        .withId(lineItemId)
        .withLotId(lotId)
        .withOrderableId(orderableId)
        .withQuantityShipped(quantityShipped)
        .build();
  }

  @Test
  public void shouldImplementToString() {
    ShipmentLineItem shipmentLineItem = new ShipmentLineItemDataBuilder().build();
    ToStringTestUtils.verify(ShipmentLineItem.class, shipmentLineItem);
  }

}