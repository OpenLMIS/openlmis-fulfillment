package org.openlmis.fulfillment.web;


import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.dto.OrderDto;
import org.openlmis.fulfillment.dto.OrderLineItemDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderDtoBuilder {

  /**
   * Crete new list of OrderDto based on list of {@link Order}
   * @param orders list on orders
   * @return list of OrderDto.
   */
  public Iterable<OrderDto> build(Iterable<Order> orders) {
    List<OrderDto> orderDtos = new ArrayList<>();
    for (Order order: orders) {
      orderDtos.add(build(order));
    }
    return orderDtos;
  }

  /**
   * Create new instance of Order based on given {@link Order}
   * @param order instance of Order
   * @return new instance od OrderDto.
   */
  public OrderDto build(Order order) {
    OrderDto orderDto =  new OrderDto();
    order.export(orderDto);

    if (order.getOrderLineItems() != null) {
      order.setOrderLineItems(new ArrayList<>());
      for (OrderLineItem orderLineItem : order.getOrderLineItems()) {
        OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
        orderLineItem.export(orderLineItemDto);
        orderDto.getOrderLineItems().add(orderLineItemDto);
      }
    }

    return orderDto;
  }
}
