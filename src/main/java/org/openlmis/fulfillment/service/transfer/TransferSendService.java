/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2026 VillageReach
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

package org.openlmis.fulfillment.service.transfer;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openlmis.fulfillment.domain.CreationDetails;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderLineItemExtraDataEntity;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.OrderNotFoundException;
import org.openlmis.fulfillment.web.util.TransferSendDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sends stock to a transfer partner without a prior request: creates a
 * {@link org.openlmis.fulfillment.domain.OrderType#TRANSFER_PUSH} order and ships it at once
 * through {@link ShipmentService#ship}, which also creates the Proof Of Delivery and submits the
 * issue stock event. Must run in the caller's transaction, so that nothing is saved when the
 * stock event is rejected.
 */
@Service
@RequiredArgsConstructor
public class TransferSendService {

  private final OrderService orderService;

  private final ShipmentService shipmentService;

  private final OrderRepository orderRepository;

  private final DateHelper dateHelper;

  /**
   * Creates the transfer order, ships it and returns it in SHIPPED status. The request must
   * already be validated: facilities, program and line items set, quantities (packs) positive.
   *
   * @param transfer what to send, from where and to where
   * @param userId   id of the user sending the stock
   * @return the shipped order
   */
  @Transactional
  public Order send(TransferSendDto transfer, UUID userId) {
    ZonedDateTime now = dateHelper.getCurrentDateTimeWithSystemZone();

    Order order = orderService.createTransferPush(buildOrder(transfer, userId, now));

    List<ShipmentLineItem> shipmentLineItems = transfer.getLineItems()
        .stream()
        .map(line -> new ShipmentLineItem(toReference(line), line.getLotId(),
            line.getQuantity(), null))
        .collect(Collectors.toList());

    Shipment shipment = new Shipment(order, new CreationDetails(userId, now),
        transfer.getNotes(), shipmentLineItems, null);
    shipmentService.ship(shipment, userId);

    // the shipment service clears the persistence context, so return a managed copy
    UUID orderId = order.getId();
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
  }

  private Order buildOrder(TransferSendDto transfer, UUID userId, ZonedDateTime now) {
    Order order = new Order(new UpdateDetails(userId, now));
    order.setProgramId(transfer.getProgramId());
    order.setSupplyingFacilityId(transfer.getSupplyingFacilityId());
    // pushed stock is for, and received by, the receiving facility
    order.setFacilityId(transfer.getReceivingFacilityId());
    order.setRequestingFacilityId(transfer.getReceivingFacilityId());
    order.setReceivingFacilityId(transfer.getReceivingFacilityId());
    order.setCreatedById(userId);
    order.setEmergency(Boolean.FALSE);
    order.setQuotedCost(BigDecimal.ZERO);
    order.setStatusMessages(new ArrayList<>());
    order.setStatusChanges(new ArrayList<>());

    // the order has one line per orderable; lots only matter for the shipment
    Map<VersionEntityReference, Long> quantities = new LinkedHashMap<>();
    transfer.getLineItems()
        .forEach(line -> quantities.merge(toReference(line), line.getQuantity(), Long::sum));

    List<OrderLineItem> orderLineItems = new ArrayList<>();
    quantities.forEach((orderable, quantity) -> orderLineItems.add(
        new OrderLineItem(null, orderable, quantity, new OrderLineItemExtraDataEntity())));
    order.setOrderLineItems(orderLineItems);

    return order;
  }

  private static VersionEntityReference toReference(TransferSendDto.LineItem line) {
    return new VersionEntityReference(line.getOrderable().getId(),
        line.getOrderable().getVersionNumber());
  }
}
