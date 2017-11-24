package org.openlmis.fulfillment;

import org.assertj.core.util.Lists;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.StatusChange;
import org.openlmis.fulfillment.domain.StatusMessage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID externalId;
  private Boolean emergency;
  private UUID facilityId;
  private UUID processingPeriodId;
  private ZonedDateTime createdDate;
  private UUID createdById;
  private UUID programId;
  private UUID requestingFacilityId;
  private UUID receivingFacilityId;
  private UUID supplyingFacilityId;
  private String orderCode;
  private OrderStatus status;
  private BigDecimal quotedCost;
  private List<OrderLineItem> orderLineItems = Lists.newArrayList();
  private List<StatusMessage> statusMessages = Lists.emptyList();
  private List<StatusChange> statusChanges = Lists.emptyList();

  public Order build() {
    Order order = new Order(
        externalId, emergency, facilityId, processingPeriodId, createdDate, createdById, programId,
        requestingFacilityId, receivingFacilityId, supplyingFacilityId, orderCode, status,
        quotedCost, orderLineItems, statusMessages, statusChanges
    );
    order.setId(id);
    return order;
  }

}
