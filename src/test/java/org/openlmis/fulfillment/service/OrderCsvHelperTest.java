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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.service.referencedata.DispensableDto;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
  private static final String APPROVED_QUANTITY = "Approved quantity";
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
    assertTrue(csv.startsWith(ORDER_NUMBER));

    orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    csv = writeCsvFile(order, orderFileTemplate);
    assertFalse(csv.startsWith(ORDER_NUMBER));
  }

  @Test
  public void shouldExportOrderFields() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertTrue(csv.startsWith("code"));
  }

  @Test
  public void shouldExportOrderLineItemFields() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.quantity.approved", APPROVED_QUANTITY,
        true, 2, null, LINE_ITEM, "approvedQuantity", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", false, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);

    assertTrue(csv.startsWith(order.getOrderLineItems().get(0).getOrderableId()
        + ",1"));
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

    assertEquals(1, numberOfLines);
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

    assertEquals(2, numberOfLines);
  }

  @Test
  public void shouldExportOnlyIncludedColumns() throws IOException {
    List<OrderFileColumn> orderFileColumns = new ArrayList<>();
    orderFileColumns.add(new OrderFileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 2, null, LINE_ITEM, ORDERABLE, null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.approved.quantity", APPROVED_QUANTITY,
        false, 3, null, LINE_ITEM, "approvedQuantity", null, null, null));
    orderFileColumns.add(new OrderFileColumn(true, "header.order.date", ORDER_DATE,
        false, 5, "dd/MM/yy", ORDER, "createdDate", null, null, null));

    OrderFileTemplate orderFileTemplate = new OrderFileTemplate("O", true, orderFileColumns);

    String csv = writeCsvFile(order, orderFileTemplate);
    assertTrue(csv.startsWith(ORDER_NUMBER + ",Product"));
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
    assertTrue(csv.startsWith("facilityCode,productCode,productName,01/16"));
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
    assertTrue(csv.startsWith("01/16,01/01/16"));
  }

  private String writeCsvFile(Order order, OrderFileTemplate orderFileTemplate)
      throws IOException {
    StringWriter writer = new StringWriter();

    orderCsvHelper.writeCsvFile(order, orderFileTemplate, writer);

    return writer.toString();
  }

  private Order createOrder() {
    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setOrderableId(UUID.randomUUID());
    orderLineItem.setOrderedQuantity(1L);
    orderLineItem.setApprovedQuantity(1L);

    OrderLineItem zeroQuantityItem = new OrderLineItem();
    zeroQuantityItem.setOrderableId(UUID.randomUUID());
    zeroQuantityItem.setOrderedQuantity(0L);

    Order order = new Order();
    order.setOrderCode("code");
    order.setCreatedDate(ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()));
    order.setExternalId(UUID.randomUUID());
    order.setProcessingPeriodId(UUID.randomUUID());
    order.setFacilityId(UUID.randomUUID());
    order.setOrderLineItems(Arrays.asList(orderLineItem, zeroQuantityItem));

    return order;
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
