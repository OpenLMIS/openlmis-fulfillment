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

package org.openlmis.fulfillment.web.util;


import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventLineItemDto;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StockEventBuilder {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(StockEventBuilder.class);
  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventBuilder.class);

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private ValidDestinationsStockManagementService validDestinationsStockManagementService;

  @Autowired
  private DateHelper dateHelper;

  /**
   * Builds a physical inventory draft DTO from the given requisition.
   *
   * @param shipment the requisition to be used a source for the physical inventory draft
   * @return the create physical inventory draft
   */
  public StockEventDto fromShipment(Shipment shipment) {
    XLOGGER.entry(shipment);
    Profiler profiler = new Profiler("BUILD_STOCK_EVENT_FROM_SHIPMENT");
    profiler.setLogger(XLOGGER);

    LOGGER.debug("Building stock events for shipment: {}", shipment.getId());

    profiler.start("BUILD_STOCK_EVENT");
    StockEventDto stockEventDto = new StockEventDto(
        shipment.getOrder().getProgramId(), shipment.getOrder().getSupplyingFacilityId(),
        getLineItems(shipment), shipment.getCreatorId()
    );

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
    return stockEventDto;
  }

  private List<StockEventLineItemDto> getLineItems(Shipment shipment) {
    return shipment
        .getLineItems()
        .stream()
        .map(lineItem -> createLineItem(shipment, lineItem))
        .collect(Collectors.toList());
  }

  private StockEventLineItemDto createLineItem(Shipment shipment, ShipmentLineItem lineItem) {
    StockEventLineItemDto dto = new StockEventLineItemDto();
    dto.setOccurredDate(dateHelper.getCurrentDate());
    dto.setDestinationId(getDestinationId(shipment.getOrder()));

    lineItem.export(dto);

    return dto;
  }

  private UUID getDestinationId(Order order) {
    FacilityDto facility = facilityReferenceDataService.findOne(order.getReceivingFacilityId());
    Collection<ValidSourceDestinationDto> destinations = validDestinationsStockManagementService
        .getValidDestinations(order.getProgramId(), facility.getType().getId());

    return destinations
        .stream()
        .filter(elem -> elem.getNode().isRefDataFacility())
        .filter(elem -> Objects.equals(facility.getId(), elem.getNode().getReferenceId()))
        .findFirst()
        .map(elem -> elem.getNode().getId())
        .orElse(null);
  }

}
