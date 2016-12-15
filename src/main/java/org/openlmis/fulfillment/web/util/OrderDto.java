package org.openlmis.fulfillment.web.util;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@AllArgsConstructor
@NoArgsConstructor
public class OrderDto implements Order.Importer, Order.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private UUID externalId;

  @Getter
  @Setter
  private Boolean emergency;

  @Getter
  @Setter
  private UUID facilityId;

  @Getter
  @Setter
  private UUID processingPeriodId;

  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @Getter
  @Setter
  private LocalDateTime createdDate;

  @Getter
  @Setter
  private UUID createdById;

  @Getter
  @Setter
  private UUID programId;

  @Getter
  @Setter
  private UUID requestingFacilityId;

  @Getter
  @Setter
  private UUID receivingFacilityId;

  @Getter
  @Setter
  private UUID supplyingFacilityId;

  @Getter
  @Setter
  private String orderCode;

  @Getter
  @Setter
  private OrderStatus status;

  @Getter
  @Setter
  private BigDecimal quotedCost;

  @Getter
  @Setter
  private UUID supervisoryNodeId;

  @Getter
  @Setter
  private UUID supplyLineId;

  @Setter
  private List<OrderLineItemDto> orderLineItems;

  @Override
  public List<OrderLineItem.Importer> getOrderLineItems() {
    return new ArrayList<>(
        Optional.ofNullable(orderLineItems).orElse(Collections.emptyList())
    );
  }

  /**
   * Crete new list of OrderDto based on list of {@link Order}
   * @param orders list on orders
   * @return list of OrderDto.
   */
  public static Iterable<OrderDto> newInstance(Iterable<Order> orders) {
    List<OrderDto> orderDtos = new ArrayList<>();
    orders.forEach(o -> orderDtos.add(newInstance(o)));
    return orderDtos;
  }

  /**
   * Create new instance of Order based on given {@link Order}
   * @param order instance of Order
   * @return new instance od OrderDto.
   */
  public static OrderDto newInstance(Order order) {
    OrderDto orderDto =  new OrderDto();
    order.export(orderDto);

    if (order.getOrderLineItems() != null) {
      orderDto.setOrderLineItems(order.getOrderLineItems().stream()
          .map(OrderLineItemDto::newInstance).collect(Collectors.toList()));
    }
    return orderDto;
  }
}
