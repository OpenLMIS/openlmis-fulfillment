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

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.junit.Before;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ShipmentDraft;
import org.openlmis.fulfillment.domain.ShipmentDraftLineItem;
import org.openlmis.fulfillment.testutils.ShipmentDraftDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDraftLineItemDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import java.util.Collections;
import java.util.UUID;

@JaversSpringDataAuditable
public class ShipmentDraftRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<ShipmentDraft> {

  @Autowired
  private ShipmentDraftRepository shipmentDraftRepository;

  @Autowired
  private OrderRepository orderRepository;

  private Order order;
  private ShipmentDraftLineItem shipmentLineItem;

  @Override
  CrudRepository<ShipmentDraft, UUID> getRepository() {
    return shipmentDraftRepository;
  }

  @Override
  ShipmentDraft generateInstance() {
    return new ShipmentDraftDataBuilder()
        .withoutId()
        .withOrder(order)
        .withLineItems(Collections.singletonList(shipmentLineItem))
        .build();
  }

  @Before
  public void setUp() {
    order = new OrderDataBuilder()
        .withoutId()
        .withOrderedStatus()
        .build();

    order = orderRepository.save(order);

    shipmentLineItem = new ShipmentDraftLineItemDataBuilder()
        .withoutId()
        .build();
  }

}
