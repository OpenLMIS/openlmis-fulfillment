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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.FacilityTypeDto;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.stockmanagement.NodeDto;
import org.openlmis.fulfillment.web.stockmanagement.NodeDtoDataBuilder;
import org.openlmis.fulfillment.web.stockmanagement.StockEventLineItemDto;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDtoDataBuilder;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

@Getter
class StockEventBuilderFixture {
  private static final ZonedDateTime CURRENT_DATE = ZonedDateTime.now();

  private FacilityTypeDto facilityType;
  private FacilityDto facility;
  private NodeDto node;
  private ValidSourceDestinationDto destination;
  private ShipmentLineItem shipmentLineItemOne;
  private ShipmentLineItem shipmentLineItemTwo;
  private Shipment shipment;
  private Order order;

  private FacilityReferenceDataService facilityReferenceDataService;
  private ValidDestinationsStockManagementService validDestinationsStockManagementService;
  private DateHelper dateHelper;

  StockEventBuilderFixture(FacilityReferenceDataService facilities,
                           ValidDestinationsStockManagementService destinations,
                           DateHelper dateHelper) {
    this.facilityReferenceDataService = facilities;
    this.validDestinationsStockManagementService = destinations;
    this.dateHelper = dateHelper;

    facilityType = new FacilityTypeDto();
    facilityType.setId(UUID.randomUUID());

    facility = new FacilityDto();
    facility.setId(UUID.randomUUID());
    facility.setType(facilityType);

    node = new NodeDtoDataBuilder().build();
    node.setReferenceId(facility.getId());

    destination = new ValidSourceDestinationDtoDataBuilder().build();
    destination.setNode(node);

    ShipmentLineItemDataBuilder lineItemBuilder = new ShipmentLineItemDataBuilder();
    ShipmentDataBuilder shipmentBuilder = new ShipmentDataBuilder();

    shipmentLineItemOne = lineItemBuilder.build();
    shipmentLineItemTwo = lineItemBuilder.build();
    shipment = shipmentBuilder
        .withLineItems(Lists.newArrayList(shipmentLineItemOne, shipmentLineItemTwo))
        .build();

    order = shipment.getOrder();
  }

  void setUp() {
    when(facilityReferenceDataService.findOne(order.getReceivingFacilityId()))
        .thenReturn(facility);

    when(validDestinationsStockManagementService
        .getValidDestinations(order.getProgramId(), facilityType.getId()))
        .thenReturn(Collections.singletonList(destination));

    when(dateHelper.getCurrentDateTimeWithSystemZone())
        .thenReturn(CURRENT_DATE);
  }

  void assertEventLineItem(StockEventLineItemDto eventLine, ShipmentLineItem shipmentLine) {
    ShipmentLineItemDto dto = new ShipmentLineItemDto();
    shipmentLine.export(dto);

    assertThat(eventLine.getOrderableId(), is(dto.getOrderableId()));
    assertThat(eventLine.getLotId(), is(dto.getLotId()));
    assertThat(eventLine.getQuantity(), is(dto.getQuantityShipped().intValue()));
    assertThat(eventLine.getOccurredDate(), is(CURRENT_DATE.toLocalDate()));
    assertThat(eventLine.getDestinationId(), is(node.getId()));
  }
}
