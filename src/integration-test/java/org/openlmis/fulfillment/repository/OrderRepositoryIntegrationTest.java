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

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.service.OrderSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Order> {

  @Autowired
  private OrderRepository orderRepository;

  Pageable pageable = new PageRequest(0, 10);

  @Override
  protected CrudRepository<Order, UUID> getRepository() {
    return orderRepository;
  }

  @Override
  protected Order generateInstance() {
    return generateInstance(OrderStatus.FULFILLING);
  }

  private Order generateInstance(OrderStatus status) {
    return generateInstance(status, UUID.randomUUID(), UUID.randomUUID());
  }

  private Order generateInstance(UUID supplyingFacilityId) {
    return generateInstance(OrderStatus.FULFILLING, supplyingFacilityId, UUID.randomUUID());
  }

  private Order generateInstance(UUID supplyingFacilityId, UUID requestingFacilityId) {
    return generateInstance(OrderStatus.FULFILLING, supplyingFacilityId, requestingFacilityId);
  }

  private Order generateInstance(OrderStatus status, UUID supplyingFacilityId,
      UUID requestingFacilityId) {
    return new OrderDataBuilder()
        .withoutId()
        .withoutLineItems()
        .withStatus(status)
        .withSupplyingFacilityId(supplyingFacilityId)
        .withRequestingFacilityId(requestingFacilityId)
        .build();
  }

  @Test
  public void testDeleteWithLine() {
    Order instance = new OrderDataBuilder().withoutId().build();
    assertNotNull(instance);

    instance = orderRepository.save(instance);
    assertInstance(instance);
    instance.forEachLine(line -> assertNotNull(line.getId()));

    UUID instanceId = instance.getId();

    orderRepository.delete(instanceId);

    assertFalse(orderRepository.exists(instanceId));
  }

  @Test
  public void shouldFindOrderByParameters() {
    Order one = orderRepository.save(generateInstance(OrderStatus.ORDERED));
    Order two = orderRepository.save(generateInstance(OrderStatus.READY_TO_PACK));
    Order three = orderRepository.save(generateInstance(OrderStatus.FULFILLING));
    Order four = orderRepository.save(generateInstance(OrderStatus.TRANSFER_FAILED));
    Order five = orderRepository.save(generateInstance(OrderStatus.SHIPPED));

    HashSet<UUID> availableRequestingFacilities = newHashSet(one.getRequestingFacilityId(),
        two.getRequestingFacilityId(),
        three.getRequestingFacilityId(), four.getRequestingFacilityId(),
        five.getRequestingFacilityId());
    OrderSearchParams params = new OrderSearchParams();
    Page<Order> page = orderRepository
        .searchOrders(params, null, pageable, null, availableRequestingFacilities);
    assertSearchOrders(page, one, two, three, four, five);

    page = orderRepository.searchOrders(params, null, new PageRequest(1, 2), null,
        availableRequestingFacilities);
    assertSearchOrders(page, three, four);

    params.setProgramId(three.getProgramId());
    page = orderRepository.searchOrders(params, null, pageable,
        null, Collections.singleton(three.getRequestingFacilityId()));
    assertSearchOrders(page, three);

    params.setProgramId(null);
    page = orderRepository
        .searchOrders(params, asSet(four.getProcessingPeriodId()), pageable, null,
            Collections.singleton(four.getRequestingFacilityId()));
    assertSearchOrders(page, four);

    page = orderRepository
        .searchOrders(params,
            asSet(four.getProcessingPeriodId(), five.getProcessingPeriodId()), pageable, null,
            asSet(four.getRequestingFacilityId(), five.getRequestingFacilityId()));
    assertSearchOrders(page, four, five);

    params.setStatus(newHashSet(five.getStatus().name()));
    page = orderRepository
        .searchOrders(params, null, pageable, null,
            Collections.singleton(five.getRequestingFacilityId()));
    assertSearchOrders(page, five);

    params.setStatus(newHashSet(one.getStatus().name(), four.getStatus().name()));
    page = orderRepository.searchOrders(params, null, pageable,
        null, newHashSet(one.getRequestingFacilityId(), four.getRequestingFacilityId()));
    assertSearchOrders(page, one, four);

    params.setStatus(null);
    params.setSupplyingFacilityId(one.getSupplyingFacilityId());
    params.setProgramId(one.getProgramId());
    page = orderRepository.searchOrders(
        params, null, pageable, null,
        Collections.singleton(one.getRequestingFacilityId())
    );
    assertSearchOrders(page, one);

    params.setSupplyingFacilityId(two.getSupplyingFacilityId());
    params.setRequestingFacilityId(two.getRequestingFacilityId());
    params.setProgramId(two.getProgramId());
    page = orderRepository.searchOrders(
        params, null, pageable, null,
        Collections.singleton(two.getRequestingFacilityId())
    );
    assertSearchOrders(page, two);

    params.setSupplyingFacilityId(null);
    page = orderRepository.searchOrders(
        params, null, pageable, null,
        Collections.singleton(two.getRequestingFacilityId())
    );
    assertSearchOrders(page, two);
  }

  @Test
  public void shouldFindOrdersAndIgnoreRights() {
    orderRepository.save(generateInstance(OrderStatus.ORDERED));
    orderRepository.save(generateInstance(OrderStatus.READY_TO_PACK));
    orderRepository.save(generateInstance(OrderStatus.FULFILLING));

    OrderSearchParams orderSearchParams = new OrderSearchParams();
    Page<Order> list = orderRepository
        .searchOrders(orderSearchParams, null, pageable);
    assertEquals(3, list.getNumberOfElements());
  }

  @Test
  public void shouldReturnEmptyPageIfUserHasNoRightForSupplyingFacilityAndRequestingFacility() {
    orderRepository.save(generateInstance(OrderStatus.ORDERED));
    orderRepository.save(generateInstance(OrderStatus.READY_TO_PACK));
    orderRepository.save(generateInstance(OrderStatus.FULFILLING));

    OrderSearchParams orderSearchParams = new OrderSearchParams();
    Page<Order> list = orderRepository.searchOrders(orderSearchParams, null, pageable,
        Collections.emptySet(), Collections.emptySet());
    assertEquals(0, list.getNumberOfElements());
  }

  @Test
  public void shouldFindOrdersBySupplyingFacility() {
    Order one = prepareOrdersForSearchByFacility();

    OrderSearchParams params = new OrderSearchParams();
    params.setSupplyingFacilityId(one.getSupplyingFacilityId());
    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(one.getSupplyingFacilityId()), Collections.emptySet());
    assertSearchOrders(list, one);

    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.emptySet(), Collections.singleton(one.getRequestingFacilityId()));
    assertSearchOrders(list, one);
  }

  @Test
  public void shouldNotFindOrdersBySupplyingFacilityIfNoRightsForOrder() {
    Order one = prepareOrdersForSearchByFacility();

    OrderSearchParams params = new OrderSearchParams();
    params.setSupplyingFacilityId(one.getSupplyingFacilityId());
    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(UUID.randomUUID()), Collections.singleton(UUID.randomUUID()));
    assertEquals(0, list.getNumberOfElements());
    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.emptySet(), Collections.singleton(UUID.randomUUID()));
    assertEquals(0, list.getNumberOfElements());
    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(UUID.randomUUID()), Collections.emptySet());
    assertEquals(0, list.getNumberOfElements());
  }

  @Test
  public void shouldFindOrdersByRequestingFacility() {
    Order one = prepareOrdersForSearchByFacility();

    OrderSearchParams params = new OrderSearchParams();
    params.setRequestingFacilityId(one.getRequestingFacilityId());

    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(one.getSupplyingFacilityId()), Collections.emptySet());
    assertSearchOrders(list, one);

    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.emptySet(), Collections.singleton(one.getRequestingFacilityId()));
    assertSearchOrders(list, one);
  }

  @Test
  public void shouldNotFindOrdersByRequestingFacilityIfNoRightsForOrder() {
    Order one = prepareOrdersForSearchByFacility();

    OrderSearchParams params = new OrderSearchParams();
    params.setRequestingFacilityId(one.getRequestingFacilityId());
    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(UUID.randomUUID()), Collections.singleton(UUID.randomUUID()));
    assertEquals(0, list.getNumberOfElements());
    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.emptySet(), Collections.singleton(UUID.randomUUID()));
    assertEquals(0, list.getNumberOfElements());
    list = orderRepository
        .searchOrders(params, null, pageable,
            Collections.singleton(UUID.randomUUID()), Collections.emptySet());
    assertEquals(0, list.getNumberOfElements());
  }

  @Test
  public void shouldFindOrdersByRequestingAndSupplyingFacility() {
    Order one = prepareOrdersForSearchByFacility();

    OrderSearchParams params = new OrderSearchParams();
    params.setRequestingFacilityId(one.getRequestingFacilityId());
    params.setSupplyingFacilityId(one.getSupplyingFacilityId());
    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable, Collections.singleton(one.getSupplyingFacilityId()),
            Collections.emptySet());
    assertSearchOrders(list, one);

    list = orderRepository
        .searchOrders(params, null, pageable, Collections.emptySet(),
            Collections.singleton(one.getRequestingFacilityId()));
    assertSearchOrders(list, one);
  }

  @Test
  public void shouldNotFindOrdersByRequestingAndSupplyingFacilityForWrongFacility() {
    Order one = prepareOrdersForSearchByFacility();
    Order two = orderRepository.save(generateInstance(OrderStatus.ORDERED));

    OrderSearchParams params = new OrderSearchParams();
    params.setRequestingFacilityId(one.getRequestingFacilityId());
    params.setSupplyingFacilityId(two.getSupplyingFacilityId());

    Page<Order> list = orderRepository
        .searchOrders(params, null, pageable, newHashSet(one.getSupplyingFacilityId(),
            two.getSupplyingFacilityId()), Collections.emptySet());
    assertEquals(0, list.getNumberOfElements());

    params.setRequestingFacilityId(two.getRequestingFacilityId());
    params.setSupplyingFacilityId(one.getSupplyingFacilityId());

    list = orderRepository
        .searchOrders(params, null, pageable, Collections.emptySet(),
            newHashSet(one.getRequestingFacilityId(), two.getRequestingFacilityId()));
    assertEquals(0, list.getNumberOfElements());
  }

  @Test
  public void shouldSort() {
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

    HashSet<UUID> availableSupplyingFacilities = newHashSet(one.getSupplyingFacilityId(),
        two.getSupplyingFacilityId(),
        three.getSupplyingFacilityId(), four.getSupplyingFacilityId());

    OrderSearchParams params = new OrderSearchParams();
    params.setStatus(newHashSet(OrderStatus.ORDERED.name()));
    Page<Order> result = orderRepository.searchOrders(params, null,
        new PageRequest(0, 10, new Sort(Sort.Direction.DESC, "createdDate")),
        availableSupplyingFacilities, null);

    assertEquals(4, result.getContent().size());
    // They should be returned from the most recent to the least recent
    assertTrue(result.getContent().get(0).getCreatedDate()
        .isAfter(result.getContent().get(1).getCreatedDate()));
    assertTrue(result.getContent().get(1).getCreatedDate()
        .isAfter(result.getContent().get(2).getCreatedDate()));
    assertTrue(result.getContent().get(2).getCreatedDate()
        .isAfter(result.getContent().get(3).getCreatedDate()));
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
      List<UUID> uuids = orderRepository
          .getRequestingFacilities(Collections.singletonList(order.getSupplyingFacilityId()));

      assertThat(uuids, hasSize(1));
      assertThat(uuids, hasItems(order.getRequestingFacilityId()));
    }
  }

  @Test
  public void shouldRetrieveRequestingFacilitiesForMultipleSupplyingFacilities() {
    Order one = orderRepository.save(generateInstance());
    Order two = orderRepository.save(generateInstance());
    Order three = orderRepository.save(generateInstance());
    Order four = orderRepository.save(generateInstance());
    Order five = orderRepository.save(generateInstance());
    Order six = orderRepository.save(generateInstance());

    for (Order order : Lists.newArrayList(one, two, three, four, five)) {
      List<UUID> uuids = orderRepository.getRequestingFacilities(asList(
          order.getSupplyingFacilityId(), six.getSupplyingFacilityId()));

      assertThat(uuids, hasSize(2));
      assertThat(uuids, hasItems(order.getRequestingFacilityId(), six.getRequestingFacilityId()));
    }
  }

  @Test
  public void shouldRetrieveOrderByOrderCode() {

    Order one = orderRepository.save(generateInstance());
    Order two = orderRepository.save(generateInstance());

    Order found = orderRepository.findByOrderCode(one.getOrderCode());
    assertThat(found, notNullValue());
    assertThat(found.getId(), is(one.getId()));
  }

  private Order prepareOrdersForSearchByFacility() {
    orderRepository.save(generateInstance(OrderStatus.ORDERED));
    orderRepository.save(generateInstance(OrderStatus.ORDERED));
    return orderRepository.save(generateInstance(OrderStatus.ORDERED));
  }

  private void assertSearchOrders(Page<Order> actual, Order... expected) {
    assertThat(actual.getContent(), hasSize(expected.length));

    Set<UUID> actualIds = getIds(actual.getContent().stream());
    Set<UUID> expectedIds = getIds(Stream.of(expected));

    assertThat(actualIds, hasSize(expected.length));
    assertThat(actualIds, equalTo(expectedIds));
  }

  private Set<UUID> getIds(Stream<Order> stream) {
    return stream.map(BaseEntity::getId).collect(Collectors.toSet());
  }
}
