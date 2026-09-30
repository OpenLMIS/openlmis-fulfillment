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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.service.referencedata.DispensableDto;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderCsvHelperTest {

  private static final String ORDER = "order";
  private static final String LINE_ITEM = "lineItem";
  private static final String ORDERABLE = "orderable";

  private static final String ORDER_NUMBER = "Order number";
  private static final String PRODUCT_CODE = "Product code";
  private static final String ORDERED_QUANTITY = "Ordered quantity";
  private static final String PERIOD = "Period";
  private static final String ORDER_DATE = "Order date";
  private static final String HEADER_ORDERABLE = "header.orderable";
  private static final String PRODUCT = "Product";
  private static final String PROGRAM = "Program";
  private static final String RELATED_ORDERABLE = "Orderable";
  private static final String CODE = "code";

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

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

    UUID programId = order.getProgramId();
    when(programReferenceDataService.findOne(programId)).thenReturn(createProgram());

    UUID productId = order.getOrderLineItems()
        .get(0).getOrderable().getId();
    when(orderableReferenceDataService.findOne(productId)).thenReturn(createProduct());
  }

  @Test
  public void shouldIncludeHeadersIfRequired() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "", ORDER_NUMBER, true, 1, null,
        ORDER, "orderCode", null, null, null));
    FileTemplate fileTemplate = new FileTemplate("O", true, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, startsWith(ORDER_NUMBER));

    fileTemplate = new FileTemplate("O", false, TemplateType.ORDER, fileColumns);

    csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, not(startsWith(ORDER_NUMBER)));
  }

  @Test
  public void shouldExportOrderFields() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, startsWith(order.getOrderCode()));
  }

  @Test
  public void shouldExportStringFields() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, "string", "STR_VALUE", null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, startsWith("STR_VALUE"));
  }


  @Test
  public void shouldExportOrderLineItemFields() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));
    fileColumns.add(new FileColumn(true, "header.quantity.ordered", ORDERED_QUANTITY,
        true, 2, null, LINE_ITEM, "orderedQuantity", null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);

    OrderLineItem lineItem = order.getOrderLineItems().get(0);
    assertThat(
        csv,
        startsWith(lineItem.getOrderable() + "," + lineItem.getOrderedQuantity())
    );
  }

  @Test
  public void shouldExcludeZeroQuantityLineItemsIfConfiguredSo() throws IOException {
    ReflectionTestUtils.setField(orderCsvHelper, "includeZeroQuantity", false);

    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    int numberOfLines = csv.split(System.lineSeparator()).length;

    assertThat(numberOfLines, is(1));
  }

  @Test
  public void shouldIncludeZeroQuantityLineItemsIfConfiguredSo() throws IOException {
    ReflectionTestUtils.setField(orderCsvHelper, "includeZeroQuantity", true);

    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 1, null, LINE_ITEM, ORDERABLE, null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    int numberOfLines = csv.split(System.lineSeparator()).length;

    assertThat(numberOfLines, is(2));
  }

  @Test
  public void shouldExportOnlyIncludedColumns() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "header.order.number", ORDER_NUMBER,
        true, 1, null, ORDER, "orderCode", null, null, null));
    fileColumns.add(new FileColumn(true, HEADER_ORDERABLE, PRODUCT,
        true, 2, null, LINE_ITEM, ORDERABLE, null, null, null));
    fileColumns.add(new FileColumn(true, "header.ordered.quantity", ORDERED_QUANTITY,
        false, 3, null, LINE_ITEM, "orderedQuantity", null, null, null));
    fileColumns.add(new FileColumn(true, "header.order.date", ORDER_DATE,
        false, 5, "dd/MM/yy", ORDER, "createdDate", null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", true, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, startsWith(ORDER_NUMBER + ",Product"));
  }

  @Test
  public void shouldExportRelatedFields() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "header.facility.code", "Facility code",
        true, 1, null, ORDER, "facilityId", "Facility", CODE, null));
    fileColumns.add(new FileColumn(true, "header.product.code", PRODUCT_CODE,
        true, 2, null, LINE_ITEM, ORDERABLE, RELATED_ORDERABLE, "productCode", null));
    fileColumns.add(new FileColumn(true, "header.product.name", "Product name",
        true, 3, null, LINE_ITEM, ORDERABLE, RELATED_ORDERABLE, "fullProductName", null));
    fileColumns.add(new FileColumn(true, "header.period", PERIOD, true, 4,
        "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", null));
    fileColumns.add(new FileColumn(true, "header.program", PROGRAM, true, 5,
        null, ORDER, "programId", "Program", CODE, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    assertThat(csv, startsWith("facilityCode,productCode,productName,01/16,programCode"));
  }

  @Test
  public void shouldFormatDates() throws IOException {
    List<FileColumn> fileColumns = new ArrayList<>();
    fileColumns.add(new FileColumn(true, "header.period", PERIOD, true, 1,
        "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", null));
    fileColumns.add(new FileColumn(true, "header.order.date", ORDER_DATE,
        true, 2, "dd/MM/yy", ORDER, "createdDate", null, null, null));

    FileTemplate fileTemplate = new FileTemplate("O", false, TemplateType.ORDER,
        fileColumns);

    String csv = writeCsvFile(order, fileTemplate);
    String date = order.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    assertThat(csv, startsWith("01/16," + date));
  }

  @Test
  public void shouldLookUpOrderLevelRelatedObjectsOncePerExport() throws IOException {
    order = createOrderWithLineItems(3);
    mockOrderLevelRelatedObjects();

    writeCsvFile(order, new FileTemplate("O", false, TemplateType.ORDER,
        createOrderLevelRelatedColumns()));

    verify(facilityReferenceDataService, times(1)).findOne(order.getFacilityId());
    verify(periodReferenceDataService, times(1)).findOne(order.getProcessingPeriodId());
    verify(programReferenceDataService, times(1)).findOne(order.getProgramId());
  }

  @Test
  public void shouldFetchProductsInSingleBatch() throws IOException {
    order = createOrderWithLineItems(3);
    List<OrderableDto> products = order.getOrderLineItems()
        .stream()
        .map(lineItem -> createProduct(lineItem.getOrderable().getId()))
        .collect(Collectors.toList());
    when(orderableReferenceDataService.findByIds(anyCollectionOf(UUID.class)))
        .thenReturn(products);

    String csv = writeCsvFile(order, new FileTemplate("O", false, TemplateType.ORDER,
        createProductColumns()));
    assertThat(csv.split("\r\n").length, is(3));
    assertThat(csv, startsWith("productCode,productName"));

    ArgumentCaptor<Set<UUID>> captor = productIdsCaptor();
    verify(orderableReferenceDataService, times(1)).findByIds(captor.capture());
    assertThat(captor.getValue(), is(getProductIds(order)));
    verify(orderableReferenceDataService, never()).findOne(any(UUID.class));
  }

  @Test
  public void shouldLookUpProductOnceWhenMissingFromBatch() throws IOException {
    UUID productId = order.getOrderLineItems().get(0).getOrderable().getId();
    when(orderableReferenceDataService.findByIds(anyCollectionOf(UUID.class)))
        .thenReturn(Collections.emptyList());

    String csv = writeCsvFile(order, new FileTemplate("O", false, TemplateType.ORDER,
        createProductColumns()));

    verify(orderableReferenceDataService, times(1)).findOne(productId);
    assertThat(csv, startsWith("productCode,productName"));
  }

  @Test
  public void shouldNotFetchProductsWithoutProductColumns() throws IOException {
    writeCsvFile(order, new FileTemplate("O", false, TemplateType.ORDER,
        createOrderLevelRelatedColumns()));

    verify(orderableReferenceDataService, never()).findByIds(anyCollectionOf(UUID.class));
  }

  @Test
  public void shouldFetchProductsOnlyForExportedLineItems() throws IOException {
    ReflectionTestUtils.setField(orderCsvHelper, "includeZeroQuantity", false);

    writeCsvFile(order, new FileTemplate("O", false, TemplateType.ORDER,
        createProductColumns()));

    ArgumentCaptor<Set<UUID>> captor = productIdsCaptor();
    verify(orderableReferenceDataService).findByIds(captor.capture());
    assertThat(captor.getValue(), is(Collections.singleton(
        order.getOrderLineItems().get(0).getOrderable().getId())));
  }

  private String writeCsvFile(Order order, FileTemplate fileTemplate)
      throws IOException {
    StringWriter writer = new StringWriter();

    orderCsvHelper.writeCsvFile(order, fileTemplate, writer);

    return writer.toString();
  }

  private Order createOrder() {
    OrderLineItem orderLineItem1 = new OrderLineItemDataBuilder()
        .withOrderable(UUID.randomUUID(), 1L)
        .withRandomOrderedQuantity()
        .build();

    OrderLineItem orderLineItem2 = new OrderLineItemDataBuilder()
        .withOrderable(UUID.randomUUID(), 1L)
        .withOrderedQuantity(0L)
        .build();

    return new OrderDataBuilder()
        .withoutId()
        .withLineItems(orderLineItem1, orderLineItem2)
        .build();
  }

  private Order createOrderWithLineItems(int count) {
    OrderLineItem[] lineItems = new OrderLineItem[count];
    for (int i = 0; i < count; i++) {
      lineItems[i] = new OrderLineItemDataBuilder()
          .withOrderable(UUID.randomUUID(), 1L)
          .withOrderedQuantity(10L)
          .build();
    }

    return new OrderDataBuilder()
        .withoutId()
        .withLineItems(lineItems)
        .build();
  }

  private void mockOrderLevelRelatedObjects() {
    when(facilityReferenceDataService.findOne(order.getFacilityId()))
        .thenReturn(createFacility());
    when(periodReferenceDataService.findOne(order.getProcessingPeriodId()))
        .thenReturn(createPeriod());
    when(programReferenceDataService.findOne(order.getProgramId()))
        .thenReturn(createProgram());
  }

  private List<FileColumn> createOrderLevelRelatedColumns() {
    return new ArrayList<>(Arrays.asList(
        new FileColumn(true, "header.facility.code", "Facility code",
            true, 1, null, ORDER, "facilityId", "Facility", CODE, null),
        new FileColumn(true, "header.period", PERIOD, true, 2,
            "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", null),
        new FileColumn(true, "header.program", PROGRAM, true, 3,
            null, ORDER, "programId", "Program", CODE, null)));
  }

  private List<FileColumn> createProductColumns() {
    return new ArrayList<>(Arrays.asList(
        new FileColumn(true, "header.product.code", PRODUCT_CODE,
            true, 1, null, "lineItemOrderable", "id", RELATED_ORDERABLE, "productCode", null),
        new FileColumn(true, "header.product.name", "Product name",
            true, 2, null, "lineItemOrderable", "id", RELATED_ORDERABLE, "fullProductName", null)));
  }

  private Set<UUID> getProductIds(Order order) {
    return order.getOrderLineItems()
        .stream()
        .map(lineItem -> lineItem.getOrderable().getId())
        .collect(Collectors.toCollection(HashSet::new));
  }

  @SuppressWarnings("unchecked")
  private ArgumentCaptor<Set<UUID>> productIdsCaptor() {
    return ArgumentCaptor.forClass((Class<Set<UUID>>) (Class<?>) Set.class);
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

  private ProgramDto createProgram() {
    ProgramDto program = new ProgramDto();
    program.setCode("programCode");

    return program;
  }

  private OrderableDto createProduct() {
    OrderableDto product = new OrderableDto();
    product.setProductCode("productCode");
    product.setFullProductName("productName");
    product.setDispensable(new DispensableDto("each", "Each"));

    return product;
  }

  private OrderableDto createProduct(UUID id) {
    OrderableDto product = createProduct();
    product.setId(id);

    return product;
  }
}
