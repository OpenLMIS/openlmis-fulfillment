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

package org.openlmis.fulfillment;

import static org.openlmis.fulfillment.domain.OrderStatus.PICKING;
import static org.openlmis.fulfillment.domain.OrderStatus.SHIPPED;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.StatusChange;
import org.openlmis.fulfillment.domain.StatusMessage;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class OrderDataBuilder {
  private UUID id = UUID.randomUUID();
  private UUID externalId = UUID.randomUUID();
  private Boolean emergency = false;
  private UUID facilityId = UUID.randomUUID();
  private UUID processingPeriodId = UUID.randomUUID();
  private ZonedDateTime createdDate = ZonedDateTime.now();
  private UUID createdById = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID requestingFacilityId = UUID.randomUUID();
  private UUID receivingFacilityId = UUID.randomUUID();
  private UUID supplyingFacilityId = UUID.randomUUID();
  private String orderCode = "ORDER-" + RandomStringUtils.randomNumeric(8) + "R";
  private OrderStatus status = TRANSFER_FAILED;
  private BigDecimal quotedCost = new BigDecimal("1.29");
  private List<OrderLineItem> orderLineItems = Lists.newArrayList(); // check constructor
  private List<StatusMessage> statusMessages = Lists.emptyList();
  private List<StatusChange> statusChanges = Lists.emptyList();

  public OrderDataBuilder() {
    orderLineItems.add(new OrderLineItemDataBuilder().withRandomOrderedQuantity().build());
  }

  public OrderDataBuilder withoutId() {
    id = null;
    return this;
  }

  public OrderDataBuilder withoutLineItems() {
    orderLineItems.clear();
    return this;
  }

  /**
   * Sets order line items that should be in the order.
   */
  public OrderDataBuilder withLineItems(OrderLineItem... lineItems) {
    orderLineItems.clear();
    Collections.addAll(orderLineItems, lineItems);
    return this;
  }

  public OrderDataBuilder withEmergencyFlag() {
    emergency = true;
    return this;
  }

  public OrderDataBuilder withProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  public OrderDataBuilder withProcessingPeriodId(UUID processingPeriodId) {
    this.processingPeriodId = processingPeriodId;
    return this;
  }

  public OrderDataBuilder withFacilityId(UUID facilityId) {
    this.facilityId = facilityId;
    return this;
  }

  public OrderDataBuilder withRandomRequestingFacilityId() {
    this.requestingFacilityId = UUID.randomUUID();
    return this;
  }

  public OrderDataBuilder withRequestingFacilityId(UUID requestingFacilityId) {
    this.requestingFacilityId = requestingFacilityId;
    return this;
  }

  public OrderDataBuilder withSupplyingFacilityId(UUID supplyingFacilityId) {
    this.supplyingFacilityId = supplyingFacilityId;
    return this;
  }

  public OrderDataBuilder withReceivingFacilityId(UUID receivingFacilityId) {
    this.receivingFacilityId = receivingFacilityId;
    return this;
  }

  public OrderDataBuilder withPickingStatus() {
    return withStatus(PICKING);
  }

  public OrderDataBuilder withShippedStatus() {
    return withStatus(SHIPPED);
  }

  public OrderDataBuilder withStatus(OrderStatus status) {
    this.status = status;
    return this;
  }

  public OrderDataBuilder withQuotedCost(BigDecimal quotedCost) {
    this.quotedCost = quotedCost;
    return this;
  }

  public OrderDataBuilder withCreatedById(UUID createdById) {
    this.createdById = createdById;
    return this;
  }

  /**
   * Creates new instance of {@link Order} based on passed data.
   */
  public Order build() {
    Order order = new Order(
        externalId, emergency, facilityId, processingPeriodId, createdDate, createdById, programId,
        requestingFacilityId, receivingFacilityId, supplyingFacilityId, orderCode, status,
        quotedCost, orderLineItems, statusMessages, statusChanges
    );
    order.setId(id);
    order.forEachLine(line -> prepareLineItems(line, order));
    return order;
  }

  private void prepareLineItems(OrderLineItem line, Order order) {
    if (null == id) {
      line.setId(null);
    }

    line.setOrder(order);
  }

}
