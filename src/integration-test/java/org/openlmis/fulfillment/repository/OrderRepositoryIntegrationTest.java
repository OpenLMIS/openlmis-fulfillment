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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Order> {

  private static final String ORDER_REPOSITORY_INTEGRATION_TEST
      = "OrderRepositoryIntegrationTest";

  @Autowired
  private OrderRepository orderRepository;

  @Override
  protected CrudRepository<Order, UUID> getRepository() {
    return orderRepository;
  }

  @Override
  protected Order generateInstance() {
    return generateInstance(OrderStatus.PICKING);
  }

  private Order generateInstance(OrderStatus status) {
    return generateInstance(status, UUID.randomUUID(), UUID.randomUUID());
  }

  private Order generateInstance(UUID supplyingFacilityId) {
    return generateInstance(OrderStatus.PICKING, supplyingFacilityId, UUID.randomUUID());
  }

  private Order generateInstance(UUID supplyingFacilityId, UUID requestingFacilityId) {
    return generateInstance(OrderStatus.PICKING, supplyingFacilityId, requestingFacilityId);
  }

  private Order generateInstance(OrderStatus status, UUID supplyingFacilityId,
                                 UUID requestingFacilityId) {
    Order order = new Order();
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setFacilityId(UUID.randomUUID());
    order.setOrderCode(ORDER_REPOSITORY_INTEGRATION_TEST + getNextInstanceNumber());
    order.setQuotedCost(new BigDecimal("1.29"));
    order.setStatus(status);
    order.setProgramId(UUID.randomUUID());
    order.setCreatedById(UUID.randomUUID());
    order.setRequestingFacilityId(requestingFacilityId);
    order.setReceivingFacilityId(UUID.randomUUID());
    order.setSupplyingFacilityId(supplyingFacilityId);
    order.setProcessingPeriodId(UUID.randomUUID());

    return order;
  }

  @Test
  public void testDeleteWithLine() {
    Order instance = generateInstance();
    assertNotNull(instance);

    // add line
    OrderLineItem line = new OrderLineItem();
    line.setOrder(instance);
    line.setOrderedQuantity(5L);
    line.setFilledQuantity(0L);
    line.setApprovedQuantity(0L);
    line.setPacksToShip(0L);

    instance.setOrderLineItems(Lists.newArrayList(line));

    instance = orderRepository.save(instance);
    assertInstance(instance);
    assertNotNull(line.getId());

    UUID instanceId = instance.getId();

    orderRepository.delete(instanceId);

    assertFalse(orderRepository.exists(instanceId));
  }

  @Test
  public void shouldFindOrderByParameters() {
    Order one = orderRepository.save(generateInstance(OrderStatus.ORDERED));
    Order two = orderRepository.save(generateInstance(OrderStatus.IN_TRANSIT));
    Order three = orderRepository.save(generateInstance(OrderStatus.PICKING));
    Order four = orderRepository.save(generateInstance(OrderStatus.PICKED));
    Order five = orderRepository.save(generateInstance(OrderStatus.SHIPPED));

    List<Order> list = orderRepository.searchOrders(null, null, null, null, null, null, null);
    assertSearchOrders(list, one, two, three, four, five);

    list = orderRepository.searchOrders(
        one.getSupplyingFacilityId(), null, null, null, null, null, null
    );
    assertSearchOrders(list, one);

    list = orderRepository.searchOrders(
        null, two.getRequestingFacilityId(), null, null, null, null, null
    );
    assertSearchOrders(list, two);

    list = orderRepository.searchOrders(
        null, null, three.getProgramId(), null, null, null, null
    );
    assertSearchOrders(list, three);

    list = orderRepository.searchOrders(
        null, null, null, four.getProcessingPeriodId(), null, null, null
    );
    assertSearchOrders(list, four);

    list = orderRepository.searchOrders(
        null, null, null, null, EnumSet.of(five.getStatus()), null, null
    );
    assertSearchOrders(list, five);

    list = orderRepository.searchOrders(
        null, null, null, null, EnumSet.of(one.getStatus(), four.getStatus()), null, null
    );
    assertSearchOrders(list, one, four);

    // createdDate is set on insert so we need to change it and update orders
    one.setCreatedDate(ZonedDateTime.of(2017, 1, 1, 1, 1, 1, 1, ZoneId.systemDefault()));
    two.setCreatedDate(ZonedDateTime.of(2017, 2, 2, 2, 2, 2, 2, ZoneId.systemDefault()));
    three.setCreatedDate(ZonedDateTime.of(2017, 3, 3, 3, 3, 3, 3, ZoneId.systemDefault()));
    four.setCreatedDate(ZonedDateTime.of(2017, 4, 4, 4, 4, 4, 4, ZoneId.systemDefault()));
    five.setCreatedDate(ZonedDateTime.of(2017, 5, 5, 5, 5, 5, 5, ZoneId.systemDefault()));

    orderRepository.save(Lists.newArrayList(one, two, three, four, five));

    list = orderRepository.searchOrders(
        null, null, null, null, null, LocalDate.of(2017, 2, 2), LocalDate.of(2017, 4, 4)
    );
    assertSearchOrders(list, two, three, four);

    list = orderRepository.searchOrders(
        null, null, null, null, null, null, LocalDate.of(2017, 4, 4)
    );
    assertSearchOrders(list, one, two, three, four);

    list = orderRepository.searchOrders(
        null, null, null, null, null, LocalDate.of(2017, 2, 2), null
    );
    assertSearchOrders(list, two, three, four, five);
  }

  @Test
  public void shouldSortOrdersByCreatedDate() {
    final Order one = orderRepository.save(generateInstance(OrderStatus.ORDERED));
    final Order two = orderRepository.save(generateInstance(OrderStatus.ORDERED));
    final Order three = orderRepository.save(generateInstance(OrderStatus.ORDERED));
    final Order four = orderRepository.save(generateInstance(OrderStatus.ORDERED));

    two.setCreatedDate(ZonedDateTime.of(2017, 3, 29, 0, 0, 0, 0, ZoneId.systemDefault()));
    four.setCreatedDate(ZonedDateTime.of(2017, 3, 29, 1, 0, 0, 0, ZoneId.systemDefault()));
    one.setCreatedDate(ZonedDateTime.of(2017, 3, 30, 0, 0, 0, 0, ZoneId.systemDefault()));
    three.setCreatedDate(ZonedDateTime.of(2017, 4, 1, 0, 0, 0, 0, ZoneId.systemDefault()));

    orderRepository.save(one);
    orderRepository.save(two);
    orderRepository.save(three);
    orderRepository.save(four);

    List<Order> result = orderRepository.searchOrders(
        null, null, null, null, Collections.singleton(OrderStatus.ORDERED), null, null
    );

    assertEquals(4, result.size());
    // They should be returned from the most recent to the least recent
    assertTrue(result.get(0).getCreatedDate().isAfter(result.get(1).getCreatedDate()));
    assertTrue(result.get(1).getCreatedDate().isAfter(result.get(2).getCreatedDate()));
    assertTrue(result.get(2).getCreatedDate().isAfter(result.get(3).getCreatedDate()));
  }

  @Test
  public void shouldRetrieveRequestingFacilities() {
    Order one = orderRepository.save(generateInstance());
    Order two = orderRepository.save(generateInstance());
    Order three = orderRepository.save(generateInstance());
    Order four = orderRepository.save(generateInstance());
    Order five = orderRepository.save(generateInstance());

    List<UUID> uuids = orderRepository.getRequestingFacilities(null);

    assertThat(uuids, hasSize(5));
    assertThat(uuids, hasItems(
        one.getRequestingFacilityId(), two.getRequestingFacilityId(),
        three.getRequestingFacilityId(), four.getRequestingFacilityId(),
        five.getRequestingFacilityId()));
  }

  @Test
  public void shouldRetrieveDistinctRequestingFacilities() {
    UUID requestingFacilityId = UUID.randomUUID();

    orderRepository.save(generateInstance(UUID.randomUUID(), requestingFacilityId));
    orderRepository.save(generateInstance(UUID.randomUUID(), requestingFacilityId));
    Order one = orderRepository.save(generateInstance(UUID.randomUUID(), requestingFacilityId));
    Order two = orderRepository.save(generateInstance(UUID.randomUUID()));
    Order three = orderRepository.save(generateInstance(UUID.randomUUID()));

    List<UUID> uuids = orderRepository.getRequestingFacilities(null);

    assertThat(uuids, hasSize(3));
    assertThat(uuids, hasItems(
        one.getRequestingFacilityId(), two.getRequestingFacilityId(),
        three.getRequestingFacilityId()));
  }

  @Test
  public void shouldRetrieveDistinctRequestingFacilitiesForGivenSupplyingFacility() {
    Order one = orderRepository.save(generateInstance());
    Order two = orderRepository.save(generateInstance());
    Order three = orderRepository.save(generateInstance());
    Order four = orderRepository.save(generateInstance());
    Order five = orderRepository.save(generateInstance());

    for (Order order : Lists.newArrayList(one, two, three, four, five)) {
      List<UUID> uuids = orderRepository.getRequestingFacilities(order.getSupplyingFacilityId());

      assertThat(uuids, hasSize(1));
      assertThat(uuids, hasItems(order.getRequestingFacilityId()));
    }
  }

  private void assertSearchOrders(List<Order> actual, Order... expected) {
    assertThat(actual, hasSize(expected.length));

    Set<UUID> actualIds = getIds(actual.stream());
    Set<UUID> expectedIds = getIds(Stream.of(expected));

    assertThat(actualIds, hasSize(expected.length));
    assertThat(actualIds, equalTo(expectedIds));
  }

  private Set<UUID> getIds(Stream<Order> stream) {
    return stream.map(BaseEntity::getId).collect(Collectors.toSet());
  }
}
