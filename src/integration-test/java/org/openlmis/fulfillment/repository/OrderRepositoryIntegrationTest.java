package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    Order order = new Order();
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setFacilityId(UUID.randomUUID());
    order.setOrderCode(ORDER_REPOSITORY_INTEGRATION_TEST + getNextInstanceNumber());
    order.setQuotedCost(new BigDecimal("1.29"));
    order.setStatus(status);
    order.setProgramId(UUID.randomUUID());
    order.setCreatedById(UUID.randomUUID());
    order.setRequestingFacilityId(UUID.randomUUID());
    order.setReceivingFacilityId(UUID.randomUUID());
    order.setSupplyingFacilityId(UUID.randomUUID());
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

    List<Order> list = orderRepository.searchOrders(null, null, null, null, null);
    assertSearchOrders(list, one, two, three, four, five);

    list = orderRepository.searchOrders(one.getSupplyingFacilityId(), null, null, null, null);
    assertSearchOrders(list, one);

    list = orderRepository.searchOrders(null, two.getRequestingFacilityId(), null, null, null);
    assertSearchOrders(list, two);

    list = orderRepository.searchOrders(null, null, three.getProgramId(), null, null);
    assertSearchOrders(list, three);

    list = orderRepository.searchOrders(null, null, null, four.getProcessingPeriodId(), null);
    assertSearchOrders(list, four);

    list = orderRepository.searchOrders(null, null, null, null, EnumSet.of(five.getStatus()));
    assertSearchOrders(list, five);
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
