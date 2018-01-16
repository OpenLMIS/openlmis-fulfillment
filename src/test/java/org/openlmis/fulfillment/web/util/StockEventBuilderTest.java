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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class StockEventBuilderTest {

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ValidDestinationsStockManagementService validDestinationsStockManagementService;

  @Mock
  private DateHelper dateHelper;

  @InjectMocks
  private StockEventBuilder stockEventBuilder;

  private StockEventBuilderFixture fixture;

  @Before
  public void setUp() {
    fixture = new StockEventBuilderFixture(
        facilityReferenceDataService, validDestinationsStockManagementService, dateHelper
    );
    fixture.setUp();
  }

  @Test
  public void shouldCreateEventFromShipment() {
    StockEventDto event = stockEventBuilder.fromShipment(fixture.getShipment());

    assertThat(event.getFacilityId(), is(fixture.getOrder().getSupplyingFacilityId()));
    assertThat(event.getProgramId(), is(fixture.getOrder().getProgramId()));
    assertThat(event.getUserId(), is(fixture.getShipment().getCreatorId()));

    assertThat(event.getLineItems(), hasSize(2));
    fixture.assertEventLineItem(event.getLineItems().get(0), fixture.getShipmentLineItemOne());
    fixture.assertEventLineItem(event.getLineItems().get(1), fixture.getShipmentLineItemTwo());
  }
}
