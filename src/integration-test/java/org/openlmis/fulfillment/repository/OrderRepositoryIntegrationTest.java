package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
    Order order = new Order();
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setFacilityId(UUID.randomUUID());
    order.setOrderCode(ORDER_REPOSITORY_INTEGRATION_TEST + getNextInstanceNumber());
    order.setQuotedCost(new BigDecimal("1.29"));
    order.setStatus(OrderStatus.PICKING);
    order.setProgramId(UUID.randomUUID());
    order.setCreatedById(UUID.randomUUID());
    order.setRequestingFacilityId(UUID.randomUUID());
    order.setReceivingFacilityId(UUID.randomUUID());
    order.setSupplyingFacilityId(UUID.randomUUID());

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
    Order one = orderRepository.save(generateInstance());
    Order two = orderRepository.save(generateInstance());
    Order three = orderRepository.save(generateInstance());

    List<Order> list = orderRepository.searchOrders(null, null, null);

    assertThat(list, hasSize(3));
    assertThat(list, hasItem(hasProperty("id", isOneOf(one.getId(), two.getId(), three.getId()))));

    list = orderRepository.searchOrders(one.getSupplyingFacilityId(), null, null);

    assertThat(list, hasSize(1));
    assertThat(list, hasItem(hasProperty("id", is(one.getId()))));

    list = orderRepository.searchOrders(null, two.getRequestingFacilityId(), null);

    assertThat(list, hasSize(1));
    assertThat(list, hasItem(hasProperty("id", is(two.getId()))));

    list = orderRepository.searchOrders(null, null, three.getProgramId());

    assertThat(list, hasSize(1));
    assertThat(list, hasItem(hasProperty("id", is(three.getId()))));
  }
}
