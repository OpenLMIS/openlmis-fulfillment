package org.openlmis.fulfillment.web.util;


import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.StatusMessage;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
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
  private FacilityDto facility;

  @Getter
  @Setter
  private ProcessingPeriodDto processingPeriod;

  @Getter
  @Setter
  private ZonedDateTime createdDate;

  @Getter
  @Setter
  private UserDto createdBy;

  @Getter
  @Setter
  private ProgramDto program;

  @Getter
  @Setter
  private FacilityDto requestingFacility;

  @Getter
  @Setter
  private FacilityDto receivingFacility;

  @Getter
  @Setter
  private FacilityDto supplyingFacility;

  @Getter
  @Setter
  private String orderCode;

  @Getter
  @Setter
  private OrderStatus status;

  @Getter
  @Setter
  private BigDecimal quotedCost;

  @Setter
  private List<OrderLineItemDto> orderLineItems;

  @Setter
  private List<StatusMessageDto> statusMessages;

  @Override
  public List<OrderLineItem.Importer> getOrderLineItems() {
    return new ArrayList<>(
        Optional.ofNullable(orderLineItems).orElse(Collections.emptyList())
    );
  }

  @Override
  public List<StatusMessage.Importer> getStatusMessages() {
    return new ArrayList<>(Optional.ofNullable(statusMessages).orElse(Collections.emptyList()));
  }

  /**
   * Crete new list of OrderDto based on list of {@link Order}
   * @param orders list on orders
   * @return list of OrderDto.
   */
  public static Iterable<OrderDto> newInstance(Iterable<Order> orders,
                                               ExporterBuilder exporter) {
    List<OrderDto> orderDtos = new ArrayList<>();
    orders.forEach(o -> orderDtos.add(newInstance(o, exporter)));
    return orderDtos;
  }

  /**
   * Create new instance of Order based on given {@link Order}
   * @param order instance of Order
   * @return new instance od OrderDto.
   */
  public static OrderDto newInstance(Order order, ExporterBuilder exporter) {
    OrderDto orderDto =  new OrderDto();
    exporter.export(order, orderDto);

    if (order.getOrderLineItems() != null) {
      orderDto.setOrderLineItems(order.getOrderLineItems().stream()
          .map(item -> OrderLineItemDto.newInstance(item, exporter)).collect(Collectors.toList()));
    }

    if (order.getStatusMessages() != null) {
      orderDto.setStatusMessages(order.getStatusMessages().stream()
          .map(StatusMessageDto::newInstance).collect(Collectors.toList()));
    }

    return orderDto;
  }
}
