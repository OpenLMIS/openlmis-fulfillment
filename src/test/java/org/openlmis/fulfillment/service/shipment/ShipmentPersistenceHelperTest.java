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

package org.openlmis.fulfillment.service.shipment;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.FileColumnBuilder;
import org.openlmis.fulfillment.FileTemplateBuilder;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.util.DateHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ShipmentPersistenceHelperTest {

  private static final String ORDERABLE_ID = "e3fc3cf3-da18-44b0-a220-77c985202e06";
  private static final String ORDER_CODE = "O0001";
  private static final String QUANTITY_SHIPPED = "1000";

  @Value("${shipment.shippedById}")
  UUID shippedById;

  @Mock
  OrderRepository orderRepository;

  @Mock
  ShipmentService shipmentService;

  @Mock
  DateHelper dateHelper;

  @InjectMocks
  ShipmentPersistenceHelper helper;

  FileTemplate template;

  Order order;

  @Before
  public void setup() {
    FileTemplateBuilder templateBuilder = new FileTemplateBuilder();
    FileColumnBuilder columnBuilder = new FileColumnBuilder();

    FileColumn orderCode = columnBuilder
        .withPosition(0).withKeyPath("orderCode").build();
    FileColumn orderableId = columnBuilder
        .withPosition(1).withKeyPath("orderableId").build();
    FileColumn quantityShipped = columnBuilder
        .withPosition(2).withKeyPath("quantityShipped").build();

    template = templateBuilder
        .withTemplateType(TemplateType.SHIPMENT)
        .withFileColumns(asList(orderCode, orderableId, quantityShipped))
        .build();

    order = new OrderDataBuilder().build();
    ReflectionTestUtils.setField(helper, "shippedById",
        UUID.randomUUID());
  }

  @Test(expected = FulfillmentException.class)
  public void shouldThrowFulfillmentExceptionWhenParsedDataIsEmpty() {
    List<Object[]> parsedData = new ArrayList<>();

    helper.createShipment(template, parsedData);
  }


  @Test(expected = FulfillmentException.class)
  public void shouldThrowFulfillmentExceptionWhenOrderIsNotFound() {
    when(orderRepository.findByOrderCode(ORDER_CODE)).thenReturn(null);

    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList(ORDER_CODE, ORDERABLE_ID, QUANTITY_SHIPPED).toArray());

    helper.createShipment(template, parsedData);
  }

  @Test
  public void shouldCallShipmentServiceSaveWhenParsingIsSuccessful() {
    when(orderRepository.findByOrderCode(ORDER_CODE)).thenReturn(order);

    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList(ORDER_CODE, ORDERABLE_ID, QUANTITY_SHIPPED).toArray());

    helper.createShipment(template, parsedData);
    verify(shipmentService).save(any());
  }


}