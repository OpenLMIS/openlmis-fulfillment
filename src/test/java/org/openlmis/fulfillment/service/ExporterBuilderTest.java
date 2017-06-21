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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.web.util.OrderLineItemDto;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class ExporterBuilderTest {

  @Mock
  private OrderableReferenceDataService products;

  @InjectMocks
  private ExporterBuilder exportBuilder;

  @Captor
  private ArgumentCaptor<Set<UUID>> argumentCaptor;

  private UUID orderableId = UUID.randomUUID();
  private OrderableDto orderableDto = mock(OrderableDto.class);
  private Order order = mock(Order.class);
  private OrderLineItem orderLineItem = mock(OrderLineItem.class);

  @Before
  public void setUp() {
    when(orderLineItem.getOrderableId()).thenReturn(orderableId);
    when(orderableDto.getId()).thenReturn(orderableId);
    when(order.getOrderLineItems()).thenReturn(Collections.singletonList(orderLineItem));
  }

  @Test
  public void lineItemExportShouldSetOrderableFromProvidedList() {
    // given
    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();

    // when
    exportBuilder.export(orderLineItem, orderLineItemDto,
        Collections.singletonList(orderableDto));

    // then
    assertEquals(orderLineItemDto.getOrderable(), orderableDto);
  }

  @Test
  public void lineItemExportShouldFetchOrderableIfNoneProvided() {
    // given
    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    when(products.findOne(orderableId)).thenReturn(orderableDto);

    // when
    exportBuilder.export(orderLineItem, orderLineItemDto, Collections.emptyList());

    // then
    assertEquals(orderLineItemDto.getOrderable(), orderableDto);
    verify(products, times(1)).findOne(orderableId);
  }

  @Test
  public void shouldGetLineItemOrderables() {
    // given
    when(products.findByIds(argumentCaptor.capture())).thenReturn(
        Collections.singletonList(orderableDto));

    // when
    List<OrderableDto> orderables = exportBuilder.getLineItemOrderables(order);

    // then
    Set<UUID> searchedIds = argumentCaptor.getValue();
    assertTrue(searchedIds.contains(orderableDto.getId()));
    assertTrue(searchedIds.size() == 1);
    assertTrue(orderables.contains(orderableDto));
  }
}
