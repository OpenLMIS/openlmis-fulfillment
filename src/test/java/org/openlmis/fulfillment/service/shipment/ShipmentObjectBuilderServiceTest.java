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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.util.DateHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ShipmentObjectBuilderServiceTest {

  private static final String ORDERABLE_ID = "e3fc3cf3-da18-44b0-a220-77c985202e06";
  private static final String ORDER_CODE = "O0001";
  private static final String QUANTITY_SHIPPED = "1000";
  private static final String BATCH_NUMBER = "1234";
  private static final UUID SHIPPED_BY_ID = UUID.randomUUID();

  private static final String ORDER_CODE_FIELD_KEY = "orderCode";
  private static final String ORDERABLE_ID_FIELD_KEY = "orderableId";
  private static final String QUANTITY_SHIPPED_FIELD_KEY = "quantityShipped";
  private static final String BATCH_NUMBER_FIELD_KEY = "batchNumber";

  @Value("${shipment.shippedById}")
  UUID shippedById;

  @Mock
  OrderRepository orderRepository;

  @Mock
  DateHelper dateHelper;

  @InjectMocks
  ShipmentObjectBuilderService builderService;

  FileTemplate template;

  Order order;

  @Before
  public void setup() {
    order = new OrderDataBuilder().withOrderCode(ORDER_CODE).build();

    ReflectionTestUtils.setField(builderService, "shippedById",
        SHIPPED_BY_ID);
  }

  @Test(expected = FulfillmentException.class)
  public void shouldThrowFulfillmentExceptionWhenParsedDataIsEmpty() {
    List<Object[]> parsedData = new ArrayList<>();

    builderService.build(template, parsedData);
  }


  @Test(expected = FulfillmentException.class)
  public void shouldThrowFulfillmentExceptionWhenOrderIsNotFound() {
    mockTemplate(false);
    when(orderRepository.findByOrderCode(ORDER_CODE)).thenReturn(null);

    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList(ORDER_CODE, ORDERABLE_ID, QUANTITY_SHIPPED).toArray());

    builderService.build(template, parsedData);
  }

  @Test
  public void shouldCreateShipmentWithRequiredFieldsProperties() {
    mockTemplate(false);
    when(orderRepository.findByOrderCode(ORDER_CODE)).thenReturn(order);

    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList(ORDER_CODE, ORDERABLE_ID, QUANTITY_SHIPPED).toArray());

    Shipment shipment = builderService.build(template, parsedData);
    assertThat(shipment.getLineItems().size(), is(equalTo(1)));
    assertThat(shipment.getOrder(), is(equalTo(order)));
    assertThat(shipment.getShippedById(), is(equalTo(SHIPPED_BY_ID)));
    assertThat(shipment.getLineItems().get(0).getOrderableId().toString(),
        is(equalTo(ORDERABLE_ID)));
    assertThat(shipment.getLineItems().get(0).getExtraData().size(),
        is(equalTo(0)));
  }


  @Test
  public void shouldCreateShipmentWithExtraData() {
    mockTemplate(true);
    when(orderRepository.findByOrderCode(ORDER_CODE)).thenReturn(order);

    List<Object[]> parsedData = new ArrayList<>();
    parsedData.add(asList(ORDER_CODE, ORDERABLE_ID, QUANTITY_SHIPPED, BATCH_NUMBER).toArray());

    Shipment shipment = builderService.build(template, parsedData);
    assertThat(shipment.getLineItems().get(0).getExtraData().size(),
        is(equalTo(1)));
    assertThat(shipment.getLineItems().get(0).getExtraData().get(BATCH_NUMBER_FIELD_KEY),
        is(equalTo(BATCH_NUMBER)));
  }

  private void mockTemplate(Boolean includeExtraData) {
    FileTemplateBuilder templateBuilder = new FileTemplateBuilder();
    FileColumnBuilder columnBuilder = new FileColumnBuilder();

    FileColumn orderCode = columnBuilder
        .withPosition(0).withKeyPath(ORDER_CODE_FIELD_KEY).build();
    FileColumn orderableId = columnBuilder
        .withPosition(1).withKeyPath(ORDERABLE_ID_FIELD_KEY).build();
    FileColumn quantityShipped = columnBuilder
        .withPosition(2).withKeyPath(QUANTITY_SHIPPED_FIELD_KEY).build();
    FileColumn batchNumber = columnBuilder
        .withPosition(3).withKeyPath(BATCH_NUMBER_FIELD_KEY).build();

    template = templateBuilder
        .withTemplateType(TemplateType.SHIPMENT)
        .withFileColumns(
            (includeExtraData)
                ? asList(orderCode, orderableId, quantityShipped, batchNumber) :
                asList(orderCode, orderableId, quantityShipped))
        .build();
  }

}