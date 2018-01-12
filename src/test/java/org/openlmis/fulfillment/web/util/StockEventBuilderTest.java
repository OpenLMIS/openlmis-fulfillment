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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventLineItemDto;

import java.time.LocalDate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class StockEventBuilderTest {
  private static final LocalDate PERIOD_END_DATE = LocalDate.now().minusDays(1);

  private ShipmentLineItemDataBuilder lineItemBuilder = new ShipmentLineItemDataBuilder();
  private ShipmentDataBuilder shipmentBuilder = new ShipmentDataBuilder();

  private ShipmentLineItem shipmentLineItemOne = lineItemBuilder.build();
  private ShipmentLineItem shipmentLineItemTwo = lineItemBuilder.build();

  private Shipment shipment = shipmentBuilder
      .withLineItems(Lists.newArrayList(shipmentLineItemOne, shipmentLineItemTwo))
      .build();

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @InjectMocks
  private StockEventBuilder stockEventBuilder;

  @Before
  public void setUp() {
    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setEndDate(PERIOD_END_DATE);

    when(periodReferenceDataService.findOne(shipment.getOrder().getProcessingPeriodId()))
        .thenReturn(period);
  }

  @Test
  public void shouldCreateEventFromShipment() {
    StockEventDto event = stockEventBuilder.fromShipment(shipment);

    assertThat(event.getFacilityId(), is(shipment.getOrder().getSupplyingFacilityId()));
    assertThat(event.getProgramId(), is(shipment.getOrder().getProgramId()));
    assertThat(event.getUserId(), is(shipment.getCreatorId()));

    assertThat(event.getLineItems(), hasSize(2));
    assertEventLineItem(event.getLineItems().get(0), shipmentLineItemOne);
    assertEventLineItem(event.getLineItems().get(1), shipmentLineItemTwo);
  }

  private void assertEventLineItem(StockEventLineItemDto eventLine, ShipmentLineItem shipmentLine) {
    assertThat(eventLine.getOrderableId(), is(shipmentLine.getOrderableId()));
    assertThat(eventLine.getLotId(), is(shipmentLine.getLotId()));
    assertThat(eventLine.getQuantity(), is(shipmentLine.getQuantityShipped().intValue()));
    assertThat(eventLine.getOccurredDate(), is(PERIOD_END_DATE));
  }
}
