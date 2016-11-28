package org.openlmis.fulfillment.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Order> {

  private static final String ORDER_REPOSITORY_INTEGRATION_TEST
      = "OrderRepositoryIntegrationTest";

  @Autowired
  private OrderRepository orderRepository;

  @Override
  CrudRepository<Order, UUID> getRepository() {
    return orderRepository;
  }

  @Override
  Order generateInstance() {
    Order order = new Order();
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(false);
    order.setFacilityId(UUID.randomUUID());
    order.setOrderCode(ORDER_REPOSITORY_INTEGRATION_TEST);
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

    instance.setOrderLineItems(Lists.newArrayList(line));

    instance = orderRepository.save(instance);
    assertInstance(instance);
    assertNotNull(line.getId());

    UUID instanceId = instance.getId();

    orderRepository.delete(instanceId);

    assertFalse(orderRepository.exists(instanceId));
  }
}
