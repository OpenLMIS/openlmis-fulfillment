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

package org.openlmis.fulfillment.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDtoDataBuilder;
import org.openlmis.fulfillment.web.util.StockEventBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DefaultShipmentCreatePostProcessorTest {

  @Mock
  private StockEventStockManagementService stockEventStockManagementService;
  @Mock
  private StockEventBuilder stockEventBuilder;
  @InjectMocks
  private DefaultShipmentCreatePostProcessor defaultShipmentCreatePostProcessor;

  @Test
  public void shouldSendCreatedStockEvent() {
    final StockEventDto mockEvent = new StockEventDtoDataBuilder().build();

    when(stockEventBuilder.fromShipment(any(Shipment.class))).thenReturn(of(mockEvent));

    defaultShipmentCreatePostProcessor.process(mock(Shipment.class));

    verify(stockEventStockManagementService).submit(mockEvent);
  }

  @Test
  public void shouldHandleEmptyStockEvent() {
    when(stockEventBuilder.fromShipment(any(Shipment.class))).thenReturn(empty());

    defaultShipmentCreatePostProcessor.process(mock(Shipment.class));

    verifyNoInteractions(stockEventStockManagementService);
  }
}
