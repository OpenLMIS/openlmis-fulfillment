package org.openlmis.fulfillment.web;

import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.domain.Order.STATUS;
import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_NOT_FOUND;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_RETRY_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_NOREPLY;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_ORDER_CREATION_BODY;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ConfigurationSettingService;
import org.openlmis.fulfillment.service.OrderFileStorage;
import org.openlmis.fulfillment.service.OrderFtpSender;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class OrderControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orders";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";

  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String EXPORT_URL = ID_URL + "/export";
  private static final String RETRY_URL = ID_URL + "/retry";
  private static final String FINALIZE_URL = ID_URL + "/finalize";
  private static final String PRINT_URL = ID_URL + "/print";
  private static final String POD_URL = ID_URL + "/proofOfDeliveries";

  private static final String ACCESS_TOKEN = "access_token";
  private static final String REQUESTING_FACILITY = "requestingFacility";
  private static final String SUPPLYING_FACILITY = "supplyingFacility";
  private static final String PROGRAM = "program";
  private static final String FORMAT = "format";
  private static final String MESSAGE_KEY = "messageKey";

  private static final String NUMBER = "10.90";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");

  private static final String CSV = "csv";

  private UUID facility = UUID.randomUUID();
  private UUID facility1 = UUID.randomUUID();
  private UUID facility2 = UUID.randomUUID();
  private UUID program1 = UUID.randomUUID();
  private UUID program2 = UUID.randomUUID();
  private UUID period1 = UUID.randomUUID();
  private UUID period2 = UUID.randomUUID();
  private UUID product1 = UUID.randomUUID();
  private UUID product2 = UUID.randomUUID();

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private OrderFileStorage orderStorage;

  @MockBean
  private OrderFtpSender orderFtpSender;

  @MockBean
  private NotificationService notificationService;

  @MockBean
  private ConfigurationSettingService configurationSettingService;

  @MockBean
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Mock
  private ProofOfDelivery proofOfDelivery;

  private Order firstOrder = new Order();
  private Order secondOrder = new Order();
  private Order thirdOrder = new Order();

  private OrderDto firstOrderDto;

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    firstOrder = addOrder(UUID.randomUUID(), facility, period1, "orderCode", UUID.randomUUID(),
        INITIAL_USER_ID, facility, facility, facility, OrderStatus.ORDERED,
        new BigDecimal("1.29"));

    secondOrder = addOrder(UUID.randomUUID(), facility2, period1, "O2", program1, INITIAL_USER_ID,
        facility2, facility2, facility1, OrderStatus.RECEIVED, new BigDecimal(100));

    thirdOrder = addOrder(UUID.randomUUID(), facility2, period2, "O3", program2, INITIAL_USER_ID,
        facility2, facility2, facility1, OrderStatus.RECEIVED, new BigDecimal(200));

    addOrderLineItem(secondOrder, product1, 35L, 50L);

    addOrderLineItem(secondOrder, product2, 10L, 15L);

    addOrderLineItem(thirdOrder, product1, 50L, 50L);

    addOrderLineItem(thirdOrder, product2, 5L, 10L);

    OrderLineItem orderLineItem = addOrderLineItem(firstOrder, product1, 35L, 50L);

    List<OrderLineItem> orderLineItems = new ArrayList<>();
    orderLineItems.add(orderLineItem);
    firstOrder.setOrderLineItems(orderLineItems);
    firstOrder.setExternalId(secondOrder.getExternalId());

    firstOrderDto = OrderDto.newInstance(firstOrder, exporter);

    given(orderRepository.findAll()).willReturn(
        Lists.newArrayList(firstOrder, secondOrder, thirdOrder)
    );

    given(orderRepository.save(any(Order.class)))
        .willAnswer(new SaveAnswer<Order>() {

          @Override
          void extraSteps(Order obj) {
            obj.setCreatedDate(ZonedDateTime.now());
          }

        });

    given(configurationSettingService.getStringValue(FULFILLMENT_EMAIL_NOREPLY))
        .willReturn("noreply@openlmis.org");
    given(configurationSettingService.getStringValue(FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT))
        .willReturn("New order");
    given(configurationSettingService.getStringValue(FULFILLMENT_EMAIL_ORDER_CREATION_BODY))
        .willReturn("Create an order: {id} with status: {status}");
  }

  private Order addOrder(UUID requisition, UUID facility, UUID processingPeriod,
                         String orderCode, UUID program, UUID user,
                         UUID requestingFacility, UUID receivingFacility,
                         UUID supplyingFacility, OrderStatus orderStatus, BigDecimal cost) {
    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setExternalId(requisition);
    order.setEmergency(false);
    order.setFacilityId(facility);
    order.setProcessingPeriodId(processingPeriod);
    order.setOrderCode(orderCode);
    order.setQuotedCost(cost);
    order.setStatus(orderStatus);
    order.setProgramId(program);
    order.setCreatedDate(ZonedDateTime.now());
    order.setCreatedById(user);
    order.setRequestingFacilityId(requestingFacility);
    order.setReceivingFacilityId(receivingFacility);
    order.setSupplyingFacilityId(supplyingFacility);
    order.setOrderLineItems(new ArrayList<>());

    given(orderRepository.findOne(order.getId())).willReturn(order);
    given(orderRepository.exists(order.getId())).willReturn(true);

    return order;
  }

  private OrderLineItem addOrderLineItem(Order order, UUID product, Long filledQuantity,
                                         Long orderedQuantity) {
    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setId(UUID.randomUUID());
    orderLineItem.setOrder(order);
    orderLineItem.setOrderableId(product);
    orderLineItem.setOrderedQuantity(orderedQuantity);
    orderLineItem.setFilledQuantity(filledQuantity);
    orderLineItem.setApprovedQuantity(3L);
    orderLineItem.setPacksToShip(0L);

    order.getOrderLineItems().add(orderLineItem);

    return orderLineItem;
  }

  @Test
  public void shouldFinalizeOrder() {
    firstOrder.setStatus(OrderStatus.ORDERED);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .put(FINALIZE_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldNotFinalizeIfWrongOrderStatus() {
    firstOrder.setStatus(OrderStatus.SHIPPED);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .put(FINALIZE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldNotFinalizeIfOrderDoesNotExist() {
    UUID id = UUID.randomUUID();

    given(orderRepository.findOne(id)).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", id.toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .put(FINALIZE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldPrintOrderAsCsv() {
    String csvContent = restAssured.given()
        .queryParam(FORMAT, CSV)
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.startsWith("productName,filledQuantity,orderedQuantity"));
  }

  @Test
  public void shouldPrintOrderAsPdf() {
    restAssured.given()
        .queryParam(FORMAT, "pdf")
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", thirdOrder.getId().toString())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundErrorIfThereIsNoOrderToPrint() {
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    restAssured.given()
        .queryParam(FORMAT, "pdf")
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindBySupplyingFacility() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));

    given(orderRepository.searchOrders(firstOrder.getSupplyingFacilityId(), null, null, null, null))
        .willReturn(Lists.newArrayList(firstOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for (OrderDto order : response) {
      assertEquals(
          order.getSupplyingFacility().getId(),
          firstOrder.getSupplyingFacilityId());
    }
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacility() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(), null, null, null
    )).willReturn(Lists.newArrayList(firstOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for (OrderDto order : response) {
      assertEquals(
          order.getSupplyingFacility().getId(),
          firstOrder.getSupplyingFacilityId());
      assertEquals(
          order.getRequestingFacility().getId(),
          firstOrder.getRequestingFacilityId());
    }
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgram() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(UUID.fromString("5c5a6f68-8658-11e6-ae22-56b6b6499611"));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), null, null
    )).willReturn(Lists.newArrayList(firstOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for (OrderDto order : response) {
      assertEquals(
          order.getSupplyingFacility().getId(),
          firstOrder.getSupplyingFacilityId());
      assertEquals(
          order.getRequestingFacility().getId(),
          firstOrder.getRequestingFacilityId());
      assertEquals(
          order.getProgram().getId(),
          firstOrder.getProgramId());
    }
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgramAndStatus() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(UUID.fromString("5c5a6f68-8658-11e6-ae22-56b6b6499611"));
    firstOrder.setStatus(READY_TO_PACK);

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), null, EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for (OrderDto order : response) {
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
          firstOrder.getStatus()
      );
    }
  }

  @Test
  public void shouldFindBySeveralStatuses() {
    firstOrder.setStatus(READY_TO_PACK);
    secondOrder.setStatus(IN_ROUTE);

    given(orderRepository.searchOrders(null, null, null, null, EnumSet.of(READY_TO_PACK, IN_ROUTE)))
        .willReturn(Lists.newArrayList(firstOrder, secondOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(STATUS, firstOrder.getStatus().toString())
        .queryParam(STATUS, secondOrder.getStatus().toString())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertEquals(2, response.length);
    for (OrderDto order : response) {
      assertThat(order.getStatus(), isOneOf(READY_TO_PACK, IN_ROUTE));
    }

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgramAndStatusAndPeriod() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(UUID.fromString("5c5a6f68-8658-11e6-ae22-56b6b6499611"));
    firstOrder.setStatus(READY_TO_PACK);
    firstOrder.setProcessingPeriodId(UUID.fromString("4c6b05c2-894b-11e6-ae22-56b6b6499611"));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(), EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    OrderDto[] response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam("processingPeriod", firstOrder.getProcessingPeriodId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(1, response.length);
    for (OrderDto order : response) {
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
          firstOrder.getStatus()
      );
      assertEquals(
          order.getProcessingPeriod().getId(),
          firstOrder.getProcessingPeriodId()
      );
    }
  }

  @Test
  public void shouldRejectSearchRequestIfStatusIsIncorrect() {
    String response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(STATUS, "abc")
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_ORDER_INVALID_STATUS)));
  }

  @Test
  public void shouldDeleteOrder() {
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentOrder() {
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteOrderInUse() {
    List<ProofOfDelivery> pods = new ArrayList<>();
    pods.add(proofOfDelivery);
    when(proofOfDeliveryRepository.findByOrderId(firstOrder.getId())).thenReturn(pods);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnConflictCodeForExistingOrder() {
    doThrow(new DataIntegrityViolationException("This exception is required by IT"))
        .when(orderRepository)
        .delete(any(Order.class));

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(409);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateOrder() {
    firstOrder.getOrderLineItems().clear();
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrderDto = OrderDto.newInstance(firstOrder, exporter);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllOrders() {

    OrderDto[] response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto[].class);

    Iterable<OrderDto> orders = Arrays.asList(response);
    assertTrue(orders.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenOrder() {

    OrderDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderDto.class);

    assertTrue(orderRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentOrder() {
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnConflictForExistingOrderCode() {
    given(orderRepository.save(any(Order.class)))
        .willThrow(new DataIntegrityViolationException("This exception is required by IT"));

    firstOrder.getOrderLineItems().clear();
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));

    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);
    firstOrder.setOrderLineItems(null);
    firstOrderDto = OrderDto.newInstance(firstOrder, exporter);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(409);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenWhenUserHasNoRightsToCreateOrder() {
    denyUserAllRights();

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldExportOrderIfTypeIsNotSpecified() {
    String csvContent = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.startsWith("Order number,Facility code,Product code,Product name,"
        + "Approved quantity,Period,Order date"));

    String orderDate = secondOrder.getCreatedDate().format(DateTimeFormatter.ofPattern("dd/MM/yy"));

    for (OrderLineItem lineItem : secondOrder.getOrderLineItems()) {
      String string = secondOrder.getOrderCode()
          + ",facilityCode,Product Code,Product Name," + lineItem.getApprovedQuantity()
          + ",03/16," + orderDate;
      assertThat(csvContent, containsString(string));
    }
  }

  @Test
  public void shouldExportOrderIfTypeIsCsv() {
    String csvContent = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", secondOrder.getId())
        .queryParam("type", CSV)
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(200)
        .extract().body().asString();

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertTrue(csvContent.startsWith("Order number,Facility code,Product code,Product name,"
        + "Approved quantity,Period,Order date"));
  }

  @Test
  public void shouldNotExportOrderIfTypeIsDifferentThanCsv() {
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    String message = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId())
        .when()
        .get(RETRY_URL)
        .then()
        .statusCode(404)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(message, equalTo(ERROR_ORDER_NOT_FOUND));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotAllowToRetryIfOrderHasIncorrectStatus() {
    firstOrder.setStatus(READY_TO_PACK);

    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);

    String message = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId())
        .when()
        .get(RETRY_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(message, equalTo(ERROR_ORDER_RETRY_INVALID_STATUS));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldAllowToDoManuallyRetry() {
    firstOrder.setStatus(OrderStatus.TRANSFER_FAILED);

    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);

    ResultDto result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldRemoveOrdersFromSearchResultWhenUserHasNoRightsForFacility() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    secondOrder.setSupplyingFacilityId(UUID.randomUUID());
    thirdOrder.setSupplyingFacilityId(secondOrder.getSupplyingFacilityId());

    denyUserAllRightsForWarehouse(secondOrder.getSupplyingFacilityId());

    given(orderRepository.searchOrders(eq(null), eq(null), eq(null), eq(null), eq(null)))
        .willReturn(Lists.newArrayList(firstOrder, secondOrder, thirdOrder));

    Order[] response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(Order[].class);

    assertThat(response, arrayWithSize(1));
    assertThat(response[0].getId(), is(equalTo(firstOrder.getId())));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPrintRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(FORMAT, CSV)
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectExportRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", secondOrder.getId())
        .when()
        .get(EXPORT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectCreateRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectDeleteRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam("id", firstOrder.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectFinalizeRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .put(FINALIZE_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetPodsForTheGivenOrder() {
    given(proofOfDelivery.getOrder()).willReturn(firstOrder);
    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);
    given(proofOfDeliveryRepository.findByOrderId(firstOrder.getId()))
        .willReturn(Collections.singletonList(proofOfDelivery));

    ProofOfDeliveryDto[] response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(POD_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto[].class);

    assertThat(response, arrayWithSize(1));
  }

  @Test
  public void shouldRejectGetPodRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(POD_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundErrorIfOrderCannotBeFound() {
    given(orderRepository.findOne(any(UUID.class))).willReturn(null);

    String response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(POD_URL)
        .then()
        .statusCode(404)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_ORDER_NOT_FOUND)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
