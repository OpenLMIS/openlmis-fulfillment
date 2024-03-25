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

package org.openlmis.fulfillment.web;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.domain.Order.ORDER_STATUS;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.i18n.MessageKeys.ORDER_NOT_FOUND;
import static org.openlmis.fulfillment.i18n.MessageKeys.ORDER_RETRY_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.PERMISSION_MISSING;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_VIEW;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.ExternalStatus;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatsData;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ObjReferenceExpander;
import org.openlmis.fulfillment.service.OrderFileStorage;
import org.openlmis.fulfillment.service.OrderFtpSender;
import org.openlmis.fulfillment.service.OrderSearchParams;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PageDto;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ProcessingPeriodDataBuilder;
import org.openlmis.fulfillment.testutils.ProgramDataBuilder;
import org.openlmis.fulfillment.testutils.UpdateDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.UserDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.util.BasicOrderDto;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.StatusChangeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class OrderControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orders";
  private static final String BATCH_URL = RESOURCE_URL + "/batch";
  private static final String REQUESTING_FACILITIES_URL = RESOURCE_URL + "/requestingFacilities";

  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String EXPORT_URL = ID_URL + "/export";
  private static final String RETRY_URL = ID_URL + "/retry";
  private static final String PRINT_URL = ID_URL + "/print";

  private static final String REQUISITION_LESS_URL = RESOURCE_URL + "/requisitionLess";
  private static final String SEND_REQUISITION_LESS_URL = ID_URL + "/requisitionLess/send";
  private static final String NUMBER_OF_ORDERS_URL = RESOURCE_URL + "/numberOfOrdersData";
  private static final String STATUSES_STATS_DATA_URL = RESOURCE_URL + "/statusesStatsData";

  private static final String REQUESTING_FACILITY = "requestingFacilityId";
  private static final String SUPPLYING_FACILITY = "supplyingFacilityId";
  private static final String PROCESSING_PERIOD = "processingPeriodId";
  private static final String PROGRAM = "programId";
  private static final String FORMAT = "format";
  private static final String MESSAGE_KEY = "messageKey";
  private static final String PERIOD_START_DATE = "periodStartDate";
  private static final String PERIOD_END_DATE = "periodEndDate";

  private static final String CSV = "csv";
  private static final String EXPAND = "expand";
  private static final String LAST_UPDATER = "lastUpdater";

  private static final String PAGE = "page";
  private static final String SIZE = "size";

  private UUID facilityId = UUID.randomUUID();
  private UUID facility1Id = UUID.randomUUID();
  private UUID facility2Id = UUID.randomUUID();
  private UUID program1Id = UUID.randomUUID();
  private UUID program2Id = UUID.randomUUID();
  private UUID period1Id = UUID.randomUUID();
  private UUID period2Id = UUID.randomUUID();
  private UUID product1Id = UUID.randomUUID();
  private UUID product2Id = UUID.randomUUID();

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private OrderFileStorage orderStorage;

  @MockBean
  private OrderFtpSender orderFtpSender;

  @MockBean
  private FacilityReferenceDataService facilityService;

  @MockBean
  private ProgramReferenceDataService programService;

  @MockBean
  private NotificationService notificationService;

  @MockBean
  private ObjReferenceExpander objReferenceExpander;

  @MockBean
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private OrderService orderService;

  @Autowired
  private ShipmentService shipmentService;

  @SpyBean
  private UserReferenceDataService userReferenceDataService;

  @SpyBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @SpyBean
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private DateHelper dateHelper;

  private Order firstOrder;
  private Order secondOrder;
  private Order thirdOrder;

  private OrderLineItem firstOrderLineItem;

  private OrderDto firstOrderDto;
  private OrderDto secondOrderDto;

  private ProgramDto program1;
  private ProgramDto program2;
  private FacilityDto facility;
  private FacilityDto facility1;
  private FacilityDto facility2;
  private ProcessingPeriodDto period1;
  private ProcessingPeriodDto period2;

  private OrderableDto product1;
  private OrderableDto product2;

  private Pageable pageable = PageRequest.of(0, 10);

  private UserDto user = new UserDataBuilder().withId(INITIAL_USER_ID).build();

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(
        ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault()));

    program1 = new ProgramDataBuilder().withId(program1Id).build();
    program2 = new ProgramDataBuilder().withId(program2Id).build();

    period1 = new ProcessingPeriodDataBuilder().withId(period1Id).build();
    period2 = new ProcessingPeriodDataBuilder().withId(period2Id).build();

    facility = new FacilityDataBuilder()
        .withId(facilityId)
        .withSupportedPrograms(Arrays.asList(program1, program2))
        .build();
    facility1 = new FacilityDataBuilder()
        .withId(facility1Id)
        .withSupportedPrograms(Arrays.asList(program1, program2))
        .build();
    facility2 = new FacilityDataBuilder()
        .withId(facility2Id)
        .withSupportedPrograms(Arrays.asList(program1, program2))
        .build();

    when(programService.findOne(eq(program1Id))).thenReturn(program1);
    when(programService.findOne(eq(program2Id))).thenReturn(program2);
    when(programService.findByIds(anySetOf(UUID.class))).thenReturn(Arrays.asList(
        program1, program2));

    when(facilityService.findOne(eq(facilityId))).thenReturn(facility);
    when(facilityService.findOne(eq(facility1Id))).thenReturn(facility1);
    when(facilityService.findOne(eq(facility2Id))).thenReturn(facility2);
    when(facilityService.findByIds(anySetOf(UUID.class))).thenReturn(Arrays.asList(
        facility, facility1, facility2));

    EntityManager entityManager = Mockito.mock(EntityManager.class);
    ReflectionTestUtils.setField(shipmentService, "entityManager", entityManager);

    product1 = new OrderableDataBuilder().withId(product1Id).build();
    product2 = new OrderableDataBuilder().withId(product2Id).build();

    when(orderableReferenceDataService.findByIdentities(anySetOf(VersionEntityReference.class)))
        .thenReturn(Arrays.asList(product1, product2));

    when(userReferenceDataService.findByIds(anySetOf(UUID.class)))
        .thenReturn(Collections.singletonList(user));

    when(periodReferenceDataService.findByIds(anyCollectionOf(UUID.class)))
        .thenReturn(Arrays.asList(period1, period2));

    when(periodReferenceDataService.findOne(period1Id))
        .thenReturn(period1);

    when(periodReferenceDataService.findOne(period2Id))
        .thenReturn(period2);

    firstOrderLineItem = createOrderLineItem(product1Id, 50L);

    firstOrder = createOrder(
        period1Id, program1Id, facilityId, facilityId, new BigDecimal("1.29"),
        firstOrderLineItem
    );

    secondOrder = createOrder(
        period1Id, program1Id, facility2Id, facility1Id, new BigDecimal(100),
        createOrderLineItem(product1Id, 50L),
        createOrderLineItem(product2Id, 15L)
    );

    thirdOrder = createOrder(
        period2Id, program2Id, facility2Id, facility1Id, new BigDecimal(200),
        createOrderLineItem(product1Id, 50L),
        createOrderLineItem(product2Id, 10L)
    );

    firstOrder.setExternalId(secondOrder.getExternalId());

    firstOrderDto = OrderDto.newInstance(firstOrder, exporter);
    secondOrderDto = OrderDto.newInstance(secondOrder, exporter);
  }

  private Order createOrder(UUID processingPeriodId, UUID program, UUID facilityId,
      UUID supplyingFacilityId, BigDecimal cost,
      OrderLineItem... lineItems) {
    Order order = new OrderDataBuilder()
        .withProcessingPeriodId(processingPeriodId)
        .withQuotedCost(cost)
        .withProgramId(program)
        .withCreatedById(INITIAL_USER_ID)
        .withFacilityId(facilityId)
        .withRequestingFacilityId(facilityId)
        .withReceivingFacilityId(facilityId)
        .withSupplyingFacilityId(supplyingFacilityId)
        .withLineItems(lineItems)
        .withUpdateDetails(new UpdateDetailsDataBuilder()
            .withUpdaterId(UUID.randomUUID())
            .withUpdatedDate(ZonedDateTime.now())
            .build())
        .build();

    given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
    given(orderRepository.existsById(order.getId())).willReturn(true);

    return order;
  }

  private OrderLineItem createOrderLineItem(UUID product, Long orderedQuantity) {
    return new OrderLineItemDataBuilder()
        .withOrderable(product, 1L)
        .withOrderedQuantity(orderedQuantity)
        .build();
  }

  @Test
  public void shouldPrintOrderAsCsv() {
    String csvContent = restAssured.given()
        .queryParam(FORMAT, CSV)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.contains("Product Code"));
  }

  @Test
  public void shouldPrintOrderAsPdf() {
    restAssured.given()
        .queryParam(FORMAT, "pdf")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", thirdOrder.getId().toString())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundErrorIfThereIsNoOrderToPrint() {
    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.empty());

    restAssured.given()
        .queryParam(FORMAT, "pdf")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId().toString())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindOrdersByAllParameters() {
    OrderSearchParams params = new OrderSearchParams(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(),
        Sets.newHashSet(READY_TO_PACK.toString()), LocalDate.of(2018, 4, 5),
        LocalDate.of(2018, 5, 5));

    given(orderService.searchOrders(params, pageable))
        .willReturn(new PageImpl<>(Lists.newArrayList(firstOrder), pageable, 2));

    PageDto response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(PROCESSING_PERIOD, firstOrder.getProcessingPeriodId())
        .queryParam(ORDER_STATUS, READY_TO_PACK.toString())
        .queryParam(PERIOD_START_DATE, "2018-04-05")
        .queryParam(PERIOD_END_DATE, "2018-05-05")
        .queryParam(PAGE, 0)
        .queryParam(SIZE, 10)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    List<BasicOrderDto> content = getPageContent(response, BasicOrderDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(content, hasSize(1));

    for (BasicOrderDto order : content) {
      assertEquals(
          order.getSupplyingFacility().getId(),
          firstOrder.getSupplyingFacilityId());
      assertEquals(
          order.getRequestingFacility().getId(),
          firstOrder.getRequestingFacilityId());
      assertEquals(
          order.getProgram().getId(),
          firstOrder.getProgramId());
      assertEquals(
          order.getStatus(),
          firstOrder.getStatus());
      assertEquals(
          order.getId(),
          firstOrder.getId());
    }
  }

  @Test
  public void shouldThrowErrorIfOrderAlreadyExists() {
    given(orderRepository.findByExternalId(any(UUID.class))).willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    verify(orderRepository, times(1)).findByExternalId(firstOrder.getExternalId());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateOrder() {
    firstOrderDto.setStatusChanges(sampleStatusChanges());

    given(orderService.createOrder(any(OrderDto.class), eq(user.getId())))
        .willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<OrderDto> orderCaptor = ArgumentCaptor.forClass(OrderDto.class);
    verify(orderService, times(1)).createOrder(orderCaptor.capture(), eq(user.getId()));

    assertThat(orderCaptor.getAllValues().get(0).getExternalId(),
        is(firstOrderDto.getExternalId()));
  }

  @Test
  public void shouldCreateRequisitionLessOrder() {
    given(orderService.createRequisitionLessOrder(any(OrderDto.class), eq(user.getId())))
        .willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(REQUISITION_LESS_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<OrderDto> orderCaptor = ArgumentCaptor.forClass(OrderDto.class);
    verify(orderService, times(1))
        .createRequisitionLessOrder(orderCaptor.capture(), eq(user.getId()));

    assertThat(orderCaptor.getAllValues().get(0).getQuotedCost(), is(BigDecimal.ZERO));
    assertThat(orderCaptor.getAllValues().get(0).getEmergency(), is(Boolean.FALSE));
  }

  @Test
  public void shouldUpdateOrder() {
    given(orderService.updateOrder(eq(firstOrderDto.getId()), any(OrderDto.class),
        eq(user.getId()))).willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrderDto.getId())
        .body(firstOrderDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(orderService, times(1))
        .updateOrder(eq(firstOrderDto.getId()), any(OrderDto.class), eq(user.getId()));
  }

  @Test
  public void shouldSendRequisitionLessOrder() {
    given(orderService.updateOrder(eq(firstOrderDto.getId()), any(OrderDto.class),
        eq(user.getId()))).willReturn(firstOrder);

    given(orderRepository.save(any(Order.class))).willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrderDto.getId())
        .body(firstOrderDto)
        .when()
        .put(SEND_REQUISITION_LESS_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderService, times(1))
        .updateOrder(eq(firstOrderDto.getId()), any(OrderDto.class), eq(user.getId()));
    verify(orderRepository, times(1)).save(orderCaptor.capture());

    assertEquals(orderCaptor.getAllValues().size(), 1);
    assertThat(orderCaptor.getAllValues().get(0).getStatus(), is(OrderStatus.ORDERED));
  }

  @Test
  public void shouldReturnExistingOrdersInsteadOfCreatingNewOnes() {
    firstOrderDto.setExternalId(UUID.randomUUID());

    given(orderRepository.findByExternalId(firstOrderDto.getExternalId())).willReturn(null);
    given(orderRepository.findByExternalId(secondOrderDto.getExternalId())).willReturn(secondOrder);

    given(orderService.createOrder(any(OrderDto.class), eq(user.getId())))
        .willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(asList(firstOrderDto, secondOrderDto))
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(200);

    ArgumentCaptor<OrderDto> orderCaptor = ArgumentCaptor.forClass(OrderDto.class);
    verify(orderService, times(1)).createOrder(orderCaptor.capture(), eq(user.getId()));
    verify(orderRepository, times(1)).findByExternalId(secondOrderDto.getExternalId());

    assertEquals(orderCaptor.getAllValues().size(), 1);
    assertThat(orderCaptor.getAllValues().get(0).getExternalId(),
        is(firstOrderDto.getExternalId()));
  }

  @Test
  public void shouldCreateMultipleOrders() {
    given(orderService.createOrder(any(OrderDto.class), eq(user.getId())))
        .willReturn(firstOrder);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(asList(firstOrderDto, secondOrderDto))
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<OrderDto> orderCaptor = ArgumentCaptor.forClass(OrderDto.class);
    verify(orderService, times(2)).createOrder(orderCaptor.capture(), eq(user.getId()));

    assertThat(orderCaptor.getAllValues().get(0).getExternalId(),
        is(firstOrderDto.getExternalId()));
    assertThat(orderCaptor.getAllValues().get(1).getExternalId(),
        is(secondOrderDto.getExternalId()));
  }

  @Test
  public void shouldGetChosenOrder() {

    OrderDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto.class);

    assertTrue(orderRepository.existsById(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetOrderWithExpandedLastUpdater() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, firstOrder.getId())
        .queryParam(EXPAND, LAST_UPDATER)
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto.class);

    ArgumentCaptor<OrderDto> captor = ArgumentCaptor.forClass(OrderDto.class);
    verify(objReferenceExpander).expandDto(captor.capture(), eq(singleton(LAST_UPDATER)));
    assertEquals(firstOrder.getId(), captor.getValue().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentOrder() {
    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNoRightsToCreateOrder() {
    doThrow(new MissingPermissionException(ORDERS_EDIT))
        .when(permissionService).canEditOrder(any(OrderDto.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNoRightsToCreateMultipleOrders() {
    doThrow(new MissingPermissionException(ORDERS_EDIT))
        .when(permissionService).canEditOrder(any(OrderDto.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(asList(firstOrderDto, secondOrderDto))
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldExportOrderIfTypeIsNotSpecified() {
    String csvContent = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.startsWith("Order number,Facility code,Product code,Product name,"
        + "Ordered quantity,Period,Order date"));

    String orderDate = secondOrder.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    for (OrderLineItem lineItem : secondOrder.getOrderLineItems()) {
      String string = StringUtils.joinWith(",", secondOrder.getOrderCode(), facility2.getCode(),
          "Product Code", "Product Name", lineItem.getOrderedQuantity(),
          period1.getStartDate().format(DateTimeFormatter.ofPattern("MM/yy")), orderDate);
      assertThat(csvContent, containsString(string));
    }
  }

  @Test
  public void shouldExportOrderIfTypeIsCsv() {
    String csvContent = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .queryParam("type", CSV)
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.startsWith("Order number,Facility code,Product code,Product name,"
        + "Ordered quantity,Period,Order date"));
  }

  @Test
  public void shouldNotExportOrderIfTypeIsDifferentThanCsv() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .queryParam("type", "pdf")
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfThereIsNoOrderToExport() {
    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId())
        .queryParam("type", CSV)
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundErrorMessageForRetryEndpointWhenOrderDoesNotExist() {
    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.empty());

    String message = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId())
        .when()
        .get(RETRY_URL)
        .then()
        .statusCode(404)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(message, equalTo(ORDER_NOT_FOUND));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToRetryIfOrderHasIncorrectStatus() {
    firstOrder.setStatus(READY_TO_PACK);

    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.of(firstOrder));

    String message = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId())
        .when()
        .get(RETRY_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(message, equalTo(ORDER_RETRY_INVALID_STATUS));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAllowToDoManuallyRetry() {
    firstOrder.setStatus(OrderStatus.TRANSFER_FAILED);

    given(orderRepository.findById(firstOrder.getId())).willReturn(Optional.of(firstOrder));

    ResultDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId())
        .when()
        .get(RETRY_URL)
        .then()
        .statusCode(200)
        .extract()
        .body()
        .as(ResultDto.class);

    assertThat(result, is(notNullValue()));
    assertThat(result.getResult(), is(notNullValue()));
    assertThat(result.getResult(), is(instanceOf(Boolean.class)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAvailableRequestingFacilities() {
    given(orderRepository.getRequestingFacilities(null))
        .willReturn(Lists.newArrayList(facilityId, facility2Id));

    UUID[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(REQUESTING_FACILITIES_URL)
        .then()
        .extract()
        .as(UUID[].class);

    assertThat(response.length, is(equalTo(2)));
    assertThat(response[0], equalTo(facilityId));
    assertThat(response[1], equalTo(facility2Id));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAvailableRequestingFacilitiesForGivenSupplyingFacility() {
    given(orderRepository.getRequestingFacilities(singletonList(facilityId)))
        .willReturn(Lists.newArrayList(singletonList(facilityId)));
    given(orderRepository.getRequestingFacilities(singletonList(facility1Id)))
        .willReturn(Lists.newArrayList(facility2Id));

    UUID[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(SUPPLYING_FACILITY, facility1Id)
        .when()
        .get(REQUESTING_FACILITIES_URL)
        .then()
        .extract()
        .as(UUID[].class);

    assertThat(response.length, is(equalTo(1)));
    assertThat(response[0], equalTo(facility2Id));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAvailableRequestingFacilitiesForMultipleSupplyingFacilities() {
    given(orderRepository.getRequestingFacilities(asList(facilityId, facility1Id)))
        .willReturn(Lists.newArrayList(facilityId));

    UUID[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(SUPPLYING_FACILITY, facilityId)
        .queryParam(SUPPLYING_FACILITY, facility1Id)
        .when()
        .get(REQUESTING_FACILITIES_URL)
        .then()
        .extract()
        .as(UUID[].class);

    assertThat(response.length, is(equalTo(1)));
    assertThat(response[0], equalTo(facilityId));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequestWhenUserHasNoRights() {
    doThrow(new MissingPermissionException(ORDERS_VIEW))
        .when(permissionService).canViewOrder(any(Order.class));

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPrintRequestWhenUserHasNoRights() {
    doThrow(new MissingPermissionException(ORDERS_VIEW))
        .when(permissionService).canViewOrder(any(Order.class));

    String response = restAssured.given()
        .queryParam(FORMAT, CSV)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectExportRequestWhenUserHasNoRights() {
    doThrow(new MissingPermissionException(ORDERS_VIEW))
        .when(permissionService).canViewOrder(any(Order.class));

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateRequestWhenUserHasNoRights() {
    doThrow(new MissingPermissionException(ORDERS_EDIT))
        .when(permissionService).canEditOrder(any(OrderDto.class));

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNumberOfOrdersToBeExecutedAndReceivedData() {
    NumberOfOrdersData ordersData = new NumberOfOrdersData();
    ordersData.setOrdersToBeExecuted(10L);
    ordersData.setOrdersToBeReceived(1L);
    given(orderService.getOrdersData()).willReturn(ordersData);

    NumberOfOrdersData response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(NUMBER_OF_ORDERS_URL)
        .then()
        .statusCode(200)
        .extract().as(NumberOfOrdersData.class);

    assertThat(response.getOrdersToBeExecuted(), is(equalTo(10L)));
    assertThat(response.getOrdersToBeReceived(), is(equalTo(1L)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnOrderStatsData() {
    OrderStatsData statsData = new OrderStatsData();
    statsData.setFacilityId(user.getHomeFacilityId());
    Map<String, Long> statusesStats = new HashMap<>();
    String testStatus = "test_status";
    statusesStats.put(testStatus, 1L);
    statsData.setStatusesStats(statusesStats);
    given(orderService.getStatusesStatsData(user.getHomeFacilityId())).willReturn(statsData);

    OrderStatsData response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(STATUSES_STATS_DATA_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderStatsData.class);

    assertThat(response.getFacilityId(), is(equalTo(user.getHomeFacilityId())));
    assertThat(response.getStatusesStats().size(), is(equalTo(1)));
    assertThat(response.getStatusesStats().get(testStatus), is(equalTo(1L)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnOrderStatsDataWithNoDataIfNoHomeFacilityAssigned() {
    user.setHomeFacilityId(null);
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    OrderStatsData response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(STATUSES_STATS_DATA_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderStatsData.class);

    assertThat(response.getFacilityId(), is(equalTo(null)));
    assertThat(response.getStatusesStats(), is(equalTo(null)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private List<StatusChangeDto> sampleStatusChanges() {
    UserDto user = new UserDto();
    user.setUsername("user");
    user.setId(UUID.randomUUID());

    List<StatusChangeDto> list = new ArrayList<>();

    list.add(new StatusChangeDto(ExternalStatus.INITIATED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.SUBMITTED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.AUTHORIZED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.IN_APPROVAL, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.REJECTED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.SUBMITTED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.AUTHORIZED, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.IN_APPROVAL, user.getId(),
        ZonedDateTime.now(), user));
    list.add(new StatusChangeDto(ExternalStatus.APPROVED, user.getId(),
        ZonedDateTime.now(), user));

    return list;
  }
}
