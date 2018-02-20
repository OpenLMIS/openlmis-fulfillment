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


import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.ConfigurationSettingService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourceDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourcesStockManagementService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
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

import java.util.List;
import java.util.Optional;
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
  private ValidSourcesStockManagementService validSourcesStockManagementService;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private DateHelper dateHelper;

  /**
   * Builds a stock event DTO from the given shipment.
   */
  public StockEventDto fromShipment(Shipment shipment) {
    XLOGGER.entry(shipment);
    Profiler profiler = new Profiler("BUILD_STOCK_EVENT_FROM_SHIPMENT");
    profiler.setLogger(XLOGGER);

    LOGGER.debug("Building stock events for shipment: {}", shipment.getId());

    profiler.start("BUILD_STOCK_EVENT");
    StockEventDto stockEventDto = new StockEventDto(
        shipment.getProgramId(), shipment.getSupplyingFacilityId(),
        getLineItems(shipment), shipment.getShippedById()
    );

    profiler.stop().log();
    XLOGGER.exit(stockEventDto);
    return stockEventDto;
  }

  /**
   * Builds a stock event DTO from the given proof of delivery.
   */
  public StockEventDto fromProofOfDelivery(ProofOfDelivery proofOfDelivery) {
    XLOGGER.entry(proofOfDelivery);
    Profiler profiler = new Profiler("BUILD_STOCK_EVENT_FROM_POD");
    profiler.setLogger(XLOGGER);

    LOGGER.debug("Building stock events for proof of delivery: {}", proofOfDelivery.getId());

    profiler.start("BUILD_STOCK_EVENT");
    StockEventDto stockEventDto = new StockEventDto(
        proofOfDelivery.getProgramId(), proofOfDelivery.getReceivingFacilityId(),
        getLineItems(proofOfDelivery), authenticationHelper.getCurrentUser().getId()
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

  private List<StockEventLineItemDto> getLineItems(ProofOfDelivery proofOfDelivery) {
    return proofOfDelivery
        .getLineItems()
        .stream()
        .filter(line -> line.getQuantityAccepted() > 0)
        .map(lineItem -> createLineItem(proofOfDelivery, lineItem))
        .collect(Collectors.toList());
  }

  private StockEventLineItemDto createLineItem(Shipment shipment, ShipmentLineItem lineItem) {
    UUID destinationId = getDestinationId(
        shipment.getReceivingFacilityId(), shipment.getProgramId()
    );

    StockEventLineItemDto dto = new StockEventLineItemDto();
    dto.setOccurredDate(dateHelper.getCurrentDate());
    dto.setDestinationId(destinationId);

    lineItem.export(dto);

    return dto;
  }

  private StockEventLineItemDto createLineItem(ProofOfDelivery proofOfDelivery,
                                               ProofOfDeliveryLineItem lineItem) {
    UUID sourceId = getSourceId(
        proofOfDelivery.getSupplyingFacilityId(), proofOfDelivery.getProgramId()
    );

    StockEventLineItemDto dto = new StockEventLineItemDto();
    dto.setOccurredDate(proofOfDelivery.getReceivedDate());
    dto.setSourceId(sourceId);
    dto.setReasonId(configurationSettingService.getTransferInReasonId());

    lineItem.export(dto);

    return dto;
  }

  private UUID getDestinationId(UUID facilityId, UUID programId) {
    return getNodeId(facilityId, programId, validDestinationsStockManagementService);
  }

  private UUID getSourceId(UUID facilityId, UUID programId) {
    return getNodeId(facilityId, programId, validSourcesStockManagementService);
  }

  private UUID getNodeId(UUID facilityId, UUID programId,
                         ValidSourceDestinationsStockManagementService service) {
    FacilityDto facility = facilityReferenceDataService.findOne(facilityId);
    Optional<ValidSourceDestinationDto> response = service
        .findOne(programId, facility.getType().getId(), facility.getId());

    if (response.isPresent()) {
      return response.get().getNode().getId();
    }

    return null;
  }

}
