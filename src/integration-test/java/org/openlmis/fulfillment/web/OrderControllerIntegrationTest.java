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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openlmis.fulfillment.domain.Order.STATUS;
import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_NOT_FOUND;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_RETRY_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Base36EncodedOrderNumberGenerator;
import org.openlmis.fulfillment.domain.ExternalStatus;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.OrderFileStorage;
import org.openlmis.fulfillment.service.OrderFtpSender;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.PageImplRepresentation;
import org.openlmis.fulfillment.web.util.BasicOrderDto;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.StatusChangeDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class OrderControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orders";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String BATCH_URL = RESOURCE_URL + "/batch";
  private static final String REQUESTING_FACILITIES_URL = RESOURCE_URL + "/requestingFacilities";

  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String EXPORT_URL = ID_URL + "/export";
  private static final String RETRY_URL = ID_URL + "/retry";
  private static final String PRINT_URL = ID_URL + "/print";
  private static final String POD_URL = ID_URL + "/proofOfDeliveries";

  private static final String ACCESS_TOKEN = "access_token";
  private static final String REQUESTING_FACILITY = "requestingFacility";
  private static final String SUPPLYING_FACILITY = "supplyingFacility";
  private static final String PROCESSING_PERIOD = "processingPeriod";
  private static final String PROGRAM = "program";
  private static final String FORMAT = "format";
  private static final String MESSAGE_KEY = "messageKey";
  private static final String PERIOD_START_DATE = "periodStartDate";
  private static final String PERIOD_END_DATE = "periodEndDate";

  private static final String CSV = "csv";

  private static final UUID PROGRAM_ID = UUID.fromString("5c5a6f68-8658-11e6-ae22-56b6b6499611");
  private static final UUID PERIOD_ID = UUID.fromString("4c6b05c2-894b-11e6-ae22-56b6b6499611");

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
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Mock
  private ProofOfDelivery proofOfDelivery;

  private Order firstOrder = new Order();
  private Order secondOrder = new Order();
  private Order thirdOrder = new Order();

  private BasicOrderDto firstOrderDto;
  private BasicOrderDto secondOrderDto;

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    firstOrder = createOrder(
        period1, UUID.randomUUID(), facility, facility, new BigDecimal("1.29"),
        createOrderLineItem(product1, 35L, 50L)
    );

    secondOrder = createOrder(
        period1, program1, facility2, facility1, new BigDecimal(100),
        createOrderLineItem(product1, 35L, 50L),
        createOrderLineItem(product2, 10L, 15L)
    );

    thirdOrder = createOrder(
        period2, program2, facility2, facility1, new BigDecimal(200),
        createOrderLineItem(product1, 50L, 50L),
        createOrderLineItem(product2, 5L, 10L)
    );

    firstOrder.setExternalId(secondOrder.getExternalId());

    firstOrderDto = BasicOrderDto.newInstance(firstOrder, exporter);
    secondOrderDto = BasicOrderDto.newInstance(secondOrder, exporter);

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
  }

  private Order createOrder(UUID processingPeriod, UUID program, UUID facility,
                            UUID supplyingFacility, BigDecimal cost,
                            OrderLineItem... lineItems) {
    Order order = new OrderDataBuilder()
        .withProcessingPeriodId(processingPeriod)
        .withQuotedCost(cost)
        .withProgramId(program)
        .withCreatedById(INITIAL_USER_ID)
        .withFacilityId(facility)
        .withRequestingFacilityId(facility)
        .withReceivingFacilityId(facility)
        .withSupplyingFacilityId(supplyingFacility)
        .withLineItems(lineItems)
        .build();

    given(orderRepository.findOne(order.getId())).willReturn(order);
    given(orderRepository.exists(order.getId())).willReturn(true);

    return order;
  }

  private OrderLineItem createOrderLineItem(UUID product, Long filledQuantity,
                                            Long orderedQuantity) {
    return new OrderLineItemDataBuilder()
        .withOrderableId(product)
        .withOrderedQuantity(orderedQuantity)
        .withFilledQuantity(filledQuantity)
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
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

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
  public void shouldFindBySupplyingFacility() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), null, null, null, null
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<BasicOrderDto> content = getPageContent(response, BasicOrderDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(content, hasSize(1));

    for (BasicOrderDto order : content) {
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
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        null, null, null
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
    }
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgram() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), null, null
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
    }
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgramAndStatus() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);
    firstOrder.setStatus(READY_TO_PACK);

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), null, EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
          firstOrder.getStatus()
      );
    }
  }

  @Test
  public void shouldFindBySeveralStatuses() {
    firstOrder.setStatus(READY_TO_PACK);
    secondOrder.setStatus(IN_ROUTE);

    given(orderRepository.searchOrders(
        null, null, null, null, EnumSet.of(READY_TO_PACK, IN_ROUTE)
    )).willReturn(Lists.newArrayList(firstOrder, secondOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(STATUS, firstOrder.getStatus().toString())
        .queryParam(STATUS, secondOrder.getStatus().toString())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<BasicOrderDto> content = getPageContent(response, BasicOrderDto.class);

    assertThat(content, hasSize(2));

    for (BasicOrderDto order : content) {
      assertThat(order.getStatus(), isOneOf(READY_TO_PACK, IN_ROUTE));
    }

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnSearchPage() throws Exception {
    given(orderRepository.searchOrders(null, null, null, null, null))
        .willReturn(Lists.newArrayList(firstOrder, secondOrder, thirdOrder));

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<BasicOrderDto> content = getPageContent(response, BasicOrderDto.class);

    assertThat(content, hasSize(1));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindBySupplyingFacilityAndRequestingFacilityAndProgramAndStatusAndPeriod() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);
    firstOrder.setStatus(READY_TO_PACK);
    firstOrder.setProcessingPeriodId(PERIOD_ID);

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(), EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(PROCESSING_PERIOD, firstOrder.getProcessingPeriodId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
          firstOrder.getStatus()
      );
      assertEquals(
          order.getProcessingPeriod().getId(),
          firstOrder.getProcessingPeriodId()
      );
    }
  }

  @Test
  public void shouldFindOrdersByPeriodStartDate() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);
    firstOrder.setStatus(READY_TO_PACK);
    firstOrder.setProcessingPeriodId(PERIOD_ID);
    firstOrder.setCreatedDate(ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault()));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(), EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(PROCESSING_PERIOD, firstOrder.getProcessingPeriodId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .queryParam(PERIOD_START_DATE, "2017-01-01")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
          firstOrder.getStatus()
      );
      assertEquals(
          order.getProcessingPeriod().getId(),
          firstOrder.getProcessingPeriodId()
      );
    }
  }

  @Test
  public void shouldFindOrdersByPeriodEndDate() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);
    firstOrder.setStatus(READY_TO_PACK);
    firstOrder.setProcessingPeriodId(PERIOD_ID);
    firstOrder.setCreatedDate(ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault()));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(), EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(PROCESSING_PERIOD, firstOrder.getProcessingPeriodId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .queryParam(PERIOD_END_DATE, "2017-01-31")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
          firstOrder.getStatus()
      );
      assertEquals(
          order.getProcessingPeriod().getId(),
          firstOrder.getProcessingPeriodId()
      );
    }
  }

  @Test
  public void shouldFindOrdersByAllParameters() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setRequestingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrder.setProgramId(PROGRAM_ID);
    firstOrder.setStatus(READY_TO_PACK);
    firstOrder.setProcessingPeriodId(PERIOD_ID);
    firstOrder.setCreatedDate(ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault()));

    given(orderRepository.searchOrders(
        firstOrder.getSupplyingFacilityId(), firstOrder.getRequestingFacilityId(),
        firstOrder.getProgramId(), firstOrder.getProcessingPeriodId(), EnumSet.of(READY_TO_PACK)
    )).willReturn(Lists.newArrayList(firstOrder));

    PageImplRepresentation response = restAssured.given()
        .queryParam(SUPPLYING_FACILITY, firstOrder.getSupplyingFacilityId())
        .queryParam(REQUESTING_FACILITY, firstOrder.getRequestingFacilityId())
        .queryParam(PROGRAM, firstOrder.getProgramId())
        .queryParam(PROCESSING_PERIOD, firstOrder.getProcessingPeriodId())
        .queryParam(STATUS, READY_TO_PACK.toString())
        .queryParam(PERIOD_START_DATE, "2017-01-01")
        .queryParam(PERIOD_END_DATE, "2017-01-31")
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_ORDER_INVALID_STATUS)));
  }

  @Test
  public void shouldCreateOrder() {
    firstOrder.getOrderLineItems().clear();
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    firstOrderDto = BasicOrderDto.newInstance(firstOrder, exporter);
    firstOrderDto.setStatusChanges(sampleStatusChanges());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(firstOrderDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(orderCaptor.capture());

    Order savedOrder = orderCaptor.getValue();
    assertThat(savedOrder.getExternalId(), is(firstOrderDto.getExternalId()));
    assertThat(savedOrder.getUpdateDetailsEmbedded().getLastUpdaterId(),
        is(firstOrderDto.getLastUpdaterId()));

    Base36EncodedOrderNumberGenerator generator = new Base36EncodedOrderNumberGenerator();
    String expectedCode = "ORDER-" + generator.generate(savedOrder) + "R";
    assertEquals(expectedCode, savedOrder.getOrderCode());
  }

  @Test
  public void shouldCreateMultipleOrders() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(asList(firstOrderDto, secondOrderDto))
        .when()
        .post(BATCH_URL)
        .then()
        .statusCode(201);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository, times(2)).save(orderCaptor.capture());

    assertThat(orderCaptor.getAllValues().get(0).getExternalId(),
        is(firstOrderDto.getExternalId()));
    assertThat(orderCaptor.getAllValues().get(1).getExternalId(),
        is(secondOrderDto.getExternalId()));
  }

  @Test
  public void shouldGetAllOrders() {

    BasicOrderDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(BasicOrderDto[].class);

    Iterable<BasicOrderDto> orders = asList(response);
    assertTrue(orders.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
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

    assertTrue(orderRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentOrder() {
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

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
  public void shouldReturnConflictForExistingOrderCode() {
    given(orderRepository.save(any(Order.class)))
        .willThrow(new DataIntegrityViolationException("This exception is required by IT"));

    firstOrder.getOrderLineItems().clear();
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));

    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);
    firstOrder.setOrderLineItems(null);
    firstOrderDto = BasicOrderDto.newInstance(firstOrder, exporter);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
    denyUserAllRights();

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
      String string = secondOrder.getOrderCode()
          + ",facilityCode,Product Code,Product Name," + lineItem.getOrderedQuantity()
          + ",01/17," + orderDate;
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
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

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
    given(orderRepository.findOne(firstOrder.getId())).willReturn(null);

    String message = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldRemoveOrdersFromSearchResultWhenUserHasNoRightsForFacility() {
    firstOrder.setSupplyingFacilityId(UUID.fromString(FACILITY_ID));
    secondOrder.setSupplyingFacilityId(UUID.randomUUID());
    thirdOrder.setSupplyingFacilityId(secondOrder.getSupplyingFacilityId());

    denyUserAllRightsForWarehouse(secondOrder.getSupplyingFacilityId());

    given(orderRepository.searchOrders(null, null, null, null, null))
        .willReturn(Lists.newArrayList(firstOrder, secondOrder, thirdOrder));

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    List<OrderDto> content = getPageContent(response, OrderDto.class);

    assertThat(content, hasSize(1));
    assertThat(content.get(0).getId(), is(equalTo(firstOrder.getId())));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAvailableRequestingFacilities() {
    given(orderRepository.getRequestingFacilities(null))
        .willReturn(Lists.newArrayList(facility, facility2));

    UUID[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(REQUESTING_FACILITIES_URL)
        .then()
        .extract()
        .as(UUID[].class);

    assertThat(response.length, is(equalTo(2)));
    assertThat(response[0], equalTo(facility));
    assertThat(response[1], equalTo(facility2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnAvailableRequestingFacilitiesForGivenSupplyingFacility() {
    given(orderRepository.getRequestingFacilities(facility))
        .willReturn(Lists.newArrayList(facility));
    given(orderRepository.getRequestingFacilities(facility1))
        .willReturn(Lists.newArrayList(facility2));

    UUID[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam("supplyingFacility", facility1)
        .when()
        .get(REQUESTING_FACILITIES_URL)
        .then()
        .extract()
        .as(UUID[].class);

    assertThat(response.length, is(equalTo(1)));
    assertThat(response[0], equalTo(facility2));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequestWhenUserHasNoRights() {
    denyUserAllRights();

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
  public void shouldGetPodForTheGivenOrder() {
    given(proofOfDelivery.getOrder()).willReturn(firstOrder);
    given(orderRepository.findOne(firstOrder.getId())).willReturn(firstOrder);
    given(proofOfDeliveryRepository.findByOrderId(firstOrder.getId()))
        .willReturn(proofOfDelivery);

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", firstOrder.getId().toString())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(POD_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto.class);

    assertThat(response.getId(), is(equalTo(proofOfDelivery.getId())));
  }

  @Test
  public void shouldRejectGetPodRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
