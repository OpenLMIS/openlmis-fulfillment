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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.fulfillment.FileColumnBuilder;
import org.openlmis.fulfillment.FileTemplateBuilder;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.util.FileColumnKeyPath;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CSVRecord.class)
public class ShipmentLineItemBuilderTest {

  private static final String ORDERABLE_ID = "e3fc3cf3-da18-44b0-a220-77c985202e06";
  private static final String PRODUCT_CODE = "010101";
  private static final String PRODUCT_CODE_2 = "323232";
  private static final String ORDER_CODE = "O0001";
  private static final String ORDER_CODE_2 = "O0002";
  private static final String QUANTITY_SHIPPED = "1000";
  private static final String BATCH_NUMBER = "1234";

  @Mock
  OrderableReferenceDataService orderableReferenceDataService;

  FileTemplate template;

  @InjectMocks
  ShipmentLineItemBuilder builder = new ShipmentLineItemBuilder();

  CSVRecord csvRecord1 = PowerMockito.mock(CSVRecord.class);

  @Before
  public void setUp() throws Exception {
    template = mockTemplate(FileColumnKeyPath.ORDERABLE_ID);

    OrderableDto dto1 = new OrderableDto();
    dto1.setProductCode(PRODUCT_CODE);
    dto1.setId(UUID.fromString(ORDERABLE_ID));
    OrderableDto dto2 = new OrderableDto();
    dto2.setProductCode(PRODUCT_CODE_2);
    dto2.setId(UUID.randomUUID());

    when(orderableReferenceDataService.findAll()).thenReturn(asList(dto1, dto2));

    when(csvRecord1.get(0)).thenReturn(ORDER_CODE);
    when(csvRecord1.get(1)).thenReturn(ORDERABLE_ID);
    when(csvRecord1.get(2)).thenReturn(QUANTITY_SHIPPED);
    when(csvRecord1.get(3)).thenReturn(BATCH_NUMBER);
  }

  private FileTemplate mockTemplate(FileColumnKeyPath orderableField) {
    FileTemplateBuilder templateBuilder = new FileTemplateBuilder();
    FileColumnBuilder columnBuilder = new FileColumnBuilder();

    FileColumn orderCode = columnBuilder
        .withPosition(0).withNested("order")
        .withKeyPath(FileColumnKeyPath.ORDER_CODE.toString()).build();
    FileColumn orderableId = columnBuilder
        .withPosition(1).withNested("lineItem")
        .withKeyPath(orderableField.toString()).build();
    FileColumn quantityShipped = columnBuilder
        .withPosition(2).withNested("lineItem")
        .withKeyPath(FileColumnKeyPath.QUANTITY_SHIPPED.toString()).build();
    FileColumn batchNumber = columnBuilder
        .withPosition(3).withNested("lineItem")
        .withKeyPath("batchNumber").build();

    return templateBuilder
        .withTemplateType(TemplateType.SHIPMENT)
        .withFileColumns(asList(orderCode, orderableId, quantityShipped, batchNumber))
        .build();
  }

  @Test
  public void buildShouldMakeALineItem() {
    List<ShipmentLineItem> lineItems = builder.build(template, asList(csvRecord1));

    assertThat(lineItems.size(), is(1));
    assertThat(lineItems.get(0).getOrderableId().toString(), is(ORDERABLE_ID));
  }

  @Test
  public void buildShouldGetOrderableIdWhenTemplateUsesProductCode() {
    template = mockTemplate(FileColumnKeyPath.PRODUCT_CODE);
    when(csvRecord1.get(1)).thenReturn(PRODUCT_CODE);

    List<ShipmentLineItem> lineItems = builder.build(template, asList(csvRecord1));

    assertThat(lineItems.size(), is(1));
    assertThat(lineItems.get(0).getOrderableId().toString(), is(ORDERABLE_ID));
  }

  @Test(expected = FulfillmentException.class)
  public void buildShouldThrowErrorTemplateUsesProductCodeAndMissingProductCodeFound() {
    template = mockTemplate(FileColumnKeyPath.PRODUCT_CODE);
    when(csvRecord1.get(1)).thenReturn("EROROR_CODE");

    builder.build(template, asList(csvRecord1));
  }


  @Test(expected = FulfillmentException.class)
  public void throwsExceptionWhenTemplateDoesNotContainOrderableField() {
    template.setFileColumns(template.getFileColumns().stream()
        .filter(c -> FileColumnKeyPath.ORDERABLE_ID.equals(c.getFileColumnKeyPathEnum())).collect(
            Collectors.toList()));
    builder.build(template, asList(csvRecord1));
  }


  @Test(expected = FulfillmentException.class)
  public void throwsExceptionWhenTemplateMissesQuantityShippedField() {
    template.setFileColumns(template.getFileColumns().stream()
        .filter(c -> FileColumnKeyPath.QUANTITY_SHIPPED.equals(c.getFileColumnKeyPathEnum()))
        .collect(
            Collectors.toList()));
    builder.build(template, asList(csvRecord1));
  }

  @Test(expected = FulfillmentException.class)
  public void throwsExceptionWhenQuantityShippedIsLessThan0() {
    when(csvRecord1.get(2)).thenReturn("-100");

    builder.build(template, asList(csvRecord1));
  }

  @Test(expected = FulfillmentException.class)
  public void throwsExceptionWhenOrderableIdIsNotFound() {
    when(csvRecord1.get(1)).thenReturn(UUID.randomUUID().toString());

    builder.build(template, asList(csvRecord1));
  }

  @Test(expected = FulfillmentException.class)
  public void throwsExceptionWhenOrderIdIsNotSameForAllRows() {
    CSVRecord csvRecord2 = PowerMockito.mock(CSVRecord.class);
    when(csvRecord2.get(0)).thenReturn(ORDER_CODE_2);
    when(csvRecord2.get(1)).thenReturn(ORDERABLE_ID);
    when(csvRecord2.get(2)).thenReturn(QUANTITY_SHIPPED);
    when(csvRecord2.get(3)).thenReturn(BATCH_NUMBER);

    builder.build(template, asList(csvRecord1, csvRecord2));
  }


}