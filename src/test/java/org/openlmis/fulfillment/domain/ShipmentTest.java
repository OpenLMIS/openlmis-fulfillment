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
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ToStringTestUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShipmentTest {

  private UUID id = UUID.randomUUID();
  private Order order = new Order(UUID.randomUUID());
  private CreationDetails shipDetails = new CreationDetailsDataBuilder().build();
  private String notes = "some notes";

  @Test
  public void shouldCreateInstanceBasedOnImporter() {
    Shipment expected = createShipment();
    Shipment.Importer importer = new Shipment.Importer() {
      @Override
      public UUID getId() {
        return id;
      }

      @Override
      public Identifiable getOrder() {
        return order;
      }

      @Override
      public CreationDetails getShipDetails() {
        return shipDetails;
      }

      @Override
      public String getNotes() {
        return notes;
      }
    };

    Shipment actual = Shipment.newInstance(importer);

    assertThat(expected, new ReflectionEquals(actual));
  }

  @Test
  public void shouldExportValues() {
    Map<String, Object> values = new HashMap<>();
    Shipment.Exporter exporter = new Shipment.Exporter() {
      @Override
      public void setId(UUID id) {
        values.put("id", id);
      }

      @Override
      public void setOrder(Order order) {
        values.put("order", order);
      }

      @Override
      public void setShipDetails(CreationDetails shipDetails) {
        values.put("shipDetails", shipDetails);
      }

      @Override
      public void setNotes(String notes) {
        values.put("notes", notes);
      }
    };

    Shipment shipment = createShipment();
    shipment.export(exporter);

    assertThat(values, hasEntry("id", id));
    assertThat(values, hasEntry("order", order));
    assertThat(values, hasEntry("shipDetails", shipDetails));
    assertThat(values, hasEntry("notes", notes));
  }

  private Shipment createShipment() {
    return new ShipmentDataBuilder()
        .withId(id)
        .withOrder(order)
        .withShipDetails(shipDetails)
        .withNotes(notes)
        .build();
  }

  @Test
  public void shouldImplementToString() {
    Shipment shipment = new ShipmentDataBuilder().build();
    ToStringTestUtils.verify(Shipment.class, shipment);
  }

}