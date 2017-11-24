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
    return order;
  }

}
