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


import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventLineItemDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StockEventBuilder {
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(StockEventBuilder.class);
  private static final Logger LOGGER = LoggerFactory.getLogger(StockEventBuilder.class);

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

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
    LocalDate occurredDate = getOccurredDate(shipment);
    StockEventDto stockEventDto = new StockEventDto(
        shipment.getOrder().getProgramId(), shipment.getOrder().getSupplyingFacilityId(),
        fromLineItems(shipment.getLineItems(), occurredDate), shipment.getCreatorId()
    );

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
    return stockEventDto;
  }

  private LocalDate getOccurredDate(Shipment shipment) {
    return periodReferenceDataService
        .findOne(shipment.getOrder().getProcessingPeriodId())
        .getEndDate();
  }

  private List<StockEventLineItemDto> fromLineItems(List<ShipmentLineItem> lineItems,
                                                    LocalDate occurredDate) {
    return lineItems
        .stream()
        .map(lineItem -> fromLineItem(lineItem, occurredDate))
        .collect(Collectors.toList());
  }

  private StockEventLineItemDto fromLineItem(ShipmentLineItem lineItem,
                                             LocalDate occurredDate) {
    return new StockEventLineItemDto(
        lineItem.getOrderableId(), lineItem.getLotId(),
        lineItem.getQuantityShipped().intValue(), occurredDate
    );
  }

}
