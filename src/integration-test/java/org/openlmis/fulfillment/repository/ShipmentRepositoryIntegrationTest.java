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

package org.openlmis.fulfillment.repository;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.CrudRepository;

public class ShipmentRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Shipment> {

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private OrderRepository orderRepository;

  private Order order;

  @Override
  CrudRepository<Shipment, UUID> getRepository() {
    return shipmentRepository;
  }

  @Override
  Shipment generateInstance() {
    return new ShipmentDataBuilder()
        .withoutId()
        .withOrder(order)
        .withoutLineItems()
        .build();
  }

  @Before
  public void setUp() {
    order = new OrderDataBuilder()
        .withoutId()
        .withOrderedStatus()
        .build();

    order = orderRepository.save(order);
  }

  @Test
  public void shouldFindShipmentPageByOrder() {
    Shipment save = shipmentRepository.save(generateInstance());

    Page<Shipment> page = shipmentRepository.findByOrder(order, createPageable(10, 0));

    assertEquals(1, page.getContent().size());
    assertEquals(save.getId(), page.getContent().get(0).getId());

    assertEquals(10, page.getSize());
    assertEquals(0, page.getNumber());
    assertEquals(1, page.getNumberOfElements());
    assertEquals(1, page.getTotalElements());
    assertEquals(1, page.getTotalPages());
  }

}
