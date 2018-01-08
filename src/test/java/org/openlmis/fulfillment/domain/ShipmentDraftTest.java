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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.openlmis.fulfillment.testutils.ShipmentDraftDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDraftLineItemDataBuilder;
import org.openlmis.fulfillment.testutils.ToStringTestUtils;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ShipmentDraftTest {

  private UUID id = UUID.randomUUID();
  private Order order = new Order(UUID.randomUUID());
  private String notes = "some notes";

  private UUID lineItemId = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private Long quantityShipped = 15L;
  private List<ShipmentDraftLineItem> lineItems =
      Collections.singletonList(new ShipmentDraftLineItemDataBuilder()
          .withId(lineItemId)
          .withOrderableId(orderableId)
          .withLotId(lotId)
          .withQuantityShipped(quantityShipped)
          .build());

  @Test
  public void shouldCreateInstanceBasedOnImporter() {
    ShipmentDraft expected = createShipment();
    DummyShipmentDraftDto shipmentDraftDto = new DummyShipmentDraftDto(id, order, notes,
        Collections.singletonList(
            new DummyShipmentLineItemDto(lineItemId, orderableId, lotId, quantityShipped)));

    ShipmentDraft actual = ShipmentDraft.newInstance(shipmentDraftDto);

    assertThat(expected, new ReflectionEquals(actual));
  }

  @Test
  public void shouldExportValues() {
    DummyShipmentDraftDto shipmentDraftDto = new DummyShipmentDraftDto();

    ShipmentDraft shipment = createShipment();
    shipment.export(shipmentDraftDto);

    assertEquals(id, shipmentDraftDto.getId());
    assertEquals(order, shipmentDraftDto.getOrder());
    assertEquals(notes, shipmentDraftDto.getNotes());
  }

  private ShipmentDraft createShipment() {
    return new ShipmentDraftDataBuilder()
        .withId(id)
        .withOrder(order)
        .withNotes(notes)
        .withLineItems(lineItems)
        .build();
  }

  @Test
  public void shouldImplementToString() {
    ShipmentDraft shipment = new ShipmentDraftDataBuilder().build();
    ToStringTestUtils.verify(ShipmentDraft.class, shipment);
  }

}