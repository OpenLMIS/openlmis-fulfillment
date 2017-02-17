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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

public class ProofOfDeliveryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ProofOfDelivery> {

  private static final String CODE = "ProofOfDeliveryRepositoryIntegrationTest";

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private OrderRepository orderRepository;

  private Order order = new Order();
  private OrderLineItem orderLineItem = new OrderLineItem();

  @Override
  ProofOfDeliveryRepository getRepository() {
    return this.proofOfDeliveryRepository;
  }

  @Before
  public void setUp() {
    order.setOrderCode(CODE);
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setQuotedCost(new BigDecimal("1.29"));
    order.setStatus(OrderStatus.PICKING);
    order.setProgramId(UUID.randomUUID());
    order.setCreatedById(UUID.randomUUID());
    order.setRequestingFacilityId(UUID.randomUUID());
    order.setReceivingFacilityId(UUID.randomUUID());
    order.setSupplyingFacilityId(UUID.randomUUID());

    orderLineItem.setOrderableId(UUID.randomUUID());
    orderLineItem.setOrderedQuantity(100L);
    orderLineItem.setFilledQuantity(100L);
    orderLineItem.setApprovedQuantity(0L);
    orderLineItem.setPacksToShip(0L);

    order.setOrderLineItems(Lists.newArrayList(orderLineItem));

    order = orderRepository.save(order);
  }

  @Override
  ProofOfDelivery generateInstance() {
    ProofOfDelivery proofOfDelivery = new ProofOfDelivery();
    proofOfDelivery.setOrder(order);
    return proofOfDelivery;
  }

  @Test
  public void testDeleteWithLine() {
    ProofOfDelivery instance = generateInstance();
    assertNotNull(instance);

    // add line
    ProofOfDeliveryLineItem line = new ProofOfDeliveryLineItem();
    line.setOrderLineItem(orderLineItem);
    line.setQuantityShipped(100L);
    line.setQuantityReturned(100L);
    line.setQuantityReceived(100L);
    line.setReplacedProductCode("replaced product code");
    line.setNotes("Notes");

    instance.setProofOfDeliveryLineItems(Lists.newArrayList(line));

    instance = proofOfDeliveryRepository.save(instance);
    assertInstance(instance);
    assertNotNull(line.getId());

    UUID instanceId = instance.getId();

    proofOfDeliveryRepository.delete(instanceId);

    assertFalse(proofOfDeliveryRepository.exists(instanceId));
  }

  @Test
  public void shouldFindProofOfDeliveriesByOrderId() {
    //given
    Order anotherOrder = new Order();
    anotherOrder.setOrderCode("Another Code");
    anotherOrder.setExternalId(UUID.randomUUID());
    anotherOrder.setEmergency(true);
    anotherOrder.setQuotedCost(new BigDecimal("1.29"));
    anotherOrder.setStatus(OrderStatus.PICKING);
    anotherOrder.setProgramId(UUID.randomUUID());
    anotherOrder.setCreatedById(UUID.randomUUID());
    anotherOrder.setRequestingFacilityId(UUID.randomUUID());
    anotherOrder.setReceivingFacilityId(UUID.randomUUID());
    anotherOrder.setSupplyingFacilityId(UUID.randomUUID());
    orderRepository.save(anotherOrder);

    // This generates POD linked to order declared in @Before
    ProofOfDelivery instance = generateInstance();
    instance = proofOfDeliveryRepository.save(instance);

    //when
    ProofOfDelivery actual = proofOfDeliveryRepository.findByOrderId(order.getId());

    //then
    assertEquals(instance, actual);
  }
}
