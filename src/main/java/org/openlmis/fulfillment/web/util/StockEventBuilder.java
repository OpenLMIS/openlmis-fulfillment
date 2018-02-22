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


import static org.openlmis.fulfillment.i18n.MessageKeys.EVENT_MISSING_SOURCE_DESTINATION;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.ConfigurationSettingService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourceDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourcesStockManagementService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.ValidationException;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
  private OrderableReferenceDataService orderableReferenceDataService;

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
    Map<UUID, OrderableDto> orderables = getOrderables(shipment.getLineItems().stream()
        .map(ShipmentLineItem::getOrderableId)
        .collect(Collectors.toSet()));

    return shipment
        .getLineItems()
        .stream()
        .map(lineItem -> createLineItem(shipment, lineItem, orderables))
        .collect(Collectors.toList());
  }

  private List<StockEventLineItemDto> getLineItems(ProofOfDelivery proofOfDelivery) {
    Map<UUID, OrderableDto> orderables = getOrderables(proofOfDelivery.getLineItems().stream()
        .map(ProofOfDeliveryLineItem::getOrderableId)
        .collect(Collectors.toSet()));

    return proofOfDelivery
        .getLineItems()
        .stream()
        .map(lineItem -> createLineItem(proofOfDelivery, lineItem, orderables))
        .collect(Collectors.toList());
  }

  private StockEventLineItemDto createLineItem(Shipment shipment, ShipmentLineItem lineItem,
                                               Map<UUID, OrderableDto> orderables) {
    UUID destinationId = getDestinationId(
        shipment.getSupplyingFacilityId(),
        shipment.getReceivingFacilityId(),
        shipment.getProgramId()
    );

    StockEventLineItemDto dto = new StockEventLineItemDto();
    dto.setOccurredDate(dateHelper.getCurrentDate());
    dto.setDestinationId(destinationId);

    lineItem.export(dto);
    convertQuantityToDispensingUnits(dto, orderables);

    return dto;
  }

  private StockEventLineItemDto createLineItem(ProofOfDelivery proofOfDelivery,
                                               ProofOfDeliveryLineItem lineItem,
                                               Map<UUID, OrderableDto> orderables) {
    UUID sourceId = getSourceId(
        proofOfDelivery.getReceivingFacilityId(),
        proofOfDelivery.getSupplyingFacilityId(),
        proofOfDelivery.getProgramId()
    );

    StockEventLineItemDto dto = new StockEventLineItemDto();
    dto.setOccurredDate(proofOfDelivery.getReceivedDate());
    dto.setSourceId(sourceId);
    dto.setReasonId(configurationSettingService.getTransferInReasonId());

    lineItem.export(dto);
    convertQuantityToDispensingUnits(dto, orderables);

    return dto;
  }

  private Map<UUID, OrderableDto> getOrderables(Set<UUID> orderableUuids) {
    return orderableReferenceDataService
        .findByIds(orderableUuids)
        .stream()
        .collect(Collectors.toMap(OrderableDto::getId, orderable -> orderable));
  }

  private void convertQuantityToDispensingUnits(StockEventLineItemDto dto,
                                                Map<UUID, OrderableDto> orderables) {
    if (orderables.containsKey(dto.getOrderableId())) {
      Long netContent = orderables.get(dto.getOrderableId()).getNetContent();
      dto.setQuantity((int) (dto.getQuantity() * netContent));
    }
  }

  private UUID getDestinationId(UUID source, UUID destination, UUID programId) {
    return getNodeId(
        source, destination, programId, validDestinationsStockManagementService
    );
  }

  private UUID getSourceId(UUID destination, UUID source, UUID programId) {
    return getNodeId(
        destination, source, programId, validSourcesStockManagementService
    );
  }

  private UUID getNodeId(UUID fromFacilityId, UUID toFacilityId, UUID programId,
                         ValidSourceDestinationsStockManagementService service) {
    FacilityDto fromFacility = facilityReferenceDataService.findOne(fromFacilityId);
    FacilityDto toFacility = facilityReferenceDataService.findOne(toFacilityId);

    Optional<ValidSourceDestinationDto> response = service
        .findOne(programId, fromFacility.getType().getId(), toFacility.getId());

    if (response.isPresent()) {
      return response.get().getNode().getId();
    }

    throw new ValidationException(EVENT_MISSING_SOURCE_DESTINATION, toFacility.getCode());
  }

}
