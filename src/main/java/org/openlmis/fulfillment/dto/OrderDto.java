package org.openlmis.fulfillment.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.convert.LocalDateTimePersistenceConverter;

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

import javax.persistence.Convert;

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
  @Convert(converter = LocalDateTimePersistenceConverter.class)
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
}
