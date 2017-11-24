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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.service.referencedata.DispensableDto;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderCsvHelperTest {

  private static final String ORDER = "order";
  private static final String LINE_ITEM = "lineItem";
  private static final String ORDERABLE = "orderableId";

  private static final String ORDER_NUMBER = "Order number";
  private static final String PRODUCT_CODE = "Product code";
  private static final String ORDERED_QUANTITY = "Ordered quantity";
  private static final String PERIOD = "Period";
  private static final String ORDER_DATE = "Order date";
  private static final String HEADER_ORDERABLE = "header.orderable";
  private static final String PRODUCT = "Product";

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @InjectMocks
  private OrderCsvHelper orderCsvHelper;

  private Order order;

  @Before
  public void setUp() {
    order = createOrder();

    UUID facilityId = order.getFacilityId();
    when(facilityReferenceDataService.findOne(facilityId)).thenReturn(createFacility());

    UUID periodId = order.getProcessingPeriodId();
    when(periodReferenceDataService.findOne(periodId)).thenReturn(createPeriod());

    UUID productId = order.getOrderLineItems()
        .get(0).getOrderableId();
    when(orderableReferenceDataService.findOne(productId)).thenReturn(createProduct());
  }

  @Test
  public void shouldIncludeHeadersIfRequired() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "", ORDER_NUMBER, true, 1, null,
        ORDER, "orderCode", null, null, null));
    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", true, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertThat(csv, startsWith(ORDER_NUMBER));

    orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    csv = writeCsvFile(order, orderFileTemplate);
    assertThat(csv, not(startsWith(ORDER_NUMBER)));
  }

  @Test
  public void shouldExportOrderFields() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertThat(csv, startsWith(order.getOrderCode()));
  }

  @Test
  public void shouldExportOrderLineItemFields() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.quantity.ordered", ORDERED_QUANTITY,
        true, 2, null, LINE_ITEM, "orderedQuantity", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);

    OrderLineItem lineItem = order.getOrderLineItems().get(0);
    assertThat(
        csv,
        startsWith(lineItem.getOrderableId() + "," + lineItem.getOrderedQuantity())
    );
  }

  @Test
  public void shouldExcludeZeroQuantityLineItemsIfConfiguredSo() throws IOException {
    ReflectionTestUtils.setField(orderCsvHelper, "includeZeroQuantity", false);

    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    int numberOfLines = csv.split(System.lineSeparator()).length;

    assertThat(numberOfLines, is(1));
  }

  @Test
  public void shouldIncludeZeroQuantityLineItemsIfConfiguredSo() throws IOException {
    ReflectionTestUtils.setField(orderCsvHelper, "includeZeroQuantity", true);

    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    int numberOfLines = csv.split(System.lineSeparator()).length;

    assertThat(numberOfLines, is(2));
  }

  @Test
  public void shouldExportOnlyIncludedColumns() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 2, null, LINE_ITEM, ORDERABLE, null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.ordered.quantity", ORDERED_QUANTITY,
        false, 3, null, LINE_ITEM, "orderedQuantity", null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.order.date", ORDER_DATE,
        false, 5, "dd/MM/yy", ORDER, "createdDate", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", true, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertThat(csv, startsWith(ORDER_NUMBER + ",Product"));
  }

  @Test
  public void shouldExportRelatedFields() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.facility.code", "Facility code",
        true, 1, null, ORDER, "facilityId", "Facility", "code", null));
    orderFileColumns.add(new OrderFileColumn(true, "header.product.code", PRODUCT_CODE,
        true, 2, null, LINE_ITEM, ORDERABLE, "Orderable", "productCode", null));
    orderFileColumns.add(new OrderFileColumn(true, "header.product.name", "Product name",
        true, 3, null, LINE_ITEM, ORDERABLE, "Orderable", "fullProductName", null));
    orderFileColumns.add(new OrderFileColumn(true, "header.period", PERIOD, true, 4,
        "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertThat(csv, startsWith("facilityCode,productCode,productName,01/16"));
  }

  @Test
  public void shouldFormatDates() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.period", PERIOD, true, 1,
        "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", null));
    orderFileColumns.add(new OrderFileColumn(true, "header.order.date", ORDER_DATE,
        true, 2, "dd/MM/yy", ORDER, "createdDate", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    String date = order.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    assertThat(csv, startsWith("01/16," + date));
  }

  private String writeCsvFile(Order order, OrderFileTemplate orderFileTemplate)
      throws IOException {
    StringWriter writer = new StringWriter();

    orderCsvHelper.writeCsvFile(order, orderFileTemplate, writer);

    return writer.toString();
  }

  private Order createOrder() {
    OrderLineItem orderLineItem1 = new OrderLineItemDataBuilder()
        .withRandomOrderedQuantity()
        .build();

    OrderLineItem orderLineItem2 = new OrderLineItemDataBuilder()
        .withOrderedQuantity(0L)
        .build();

    return new OrderDataBuilder()
        .withoutId()
        .withLineItems(orderLineItem1, orderLineItem2)
        .build();
  }

  private FacilityDto createFacility() {
    FacilityDto facility = new FacilityDto();
    facility.setCode("facilityCode");

    return facility;
  }

  private ProcessingPeriodDto createPeriod() {
    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setName("periodName");
    period.setStartDate(LocalDate.of(2016, Month.JANUARY, 1));

    return period;
  }

  private OrderableDto createProduct() {
    OrderableDto product = new OrderableDto();
    product.setProductCode("productCode");
    product.setFullProductName("productName");
    product.setDispensable(new DispensableDto("each"));

    return product;
  }
}
