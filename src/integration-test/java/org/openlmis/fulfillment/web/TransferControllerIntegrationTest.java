/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2026 VillageReach
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.i18n.MessageKeys.PERMISSION_MISSING;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_INVALID_PAIR;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_NOTHING_TO_SEND;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_NOT_TRANSFER_REQUEST;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_REQUIRED_FIELD;
import static org.openlmis.fulfillment.service.PermissionService.ORDER_CREATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.OrderType;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.transfer.TransferDirection;
import org.openlmis.fulfillment.service.transfer.TransferPartnerService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ProgramDataBuilder;
import org.openlmis.fulfillment.testutils.UserDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderLineItemDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

@SuppressWarnings("PMD.TooManyMethods")
public class TransferControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String PARTNERS_URL = "/api/transfers/partners";
  private static final String REQUESTS_URL = "/api/transfers/requests";
  private static final String REQUEST_URL = REQUESTS_URL + "/{id}";
  private static final String SEND_URL = REQUEST_URL + "/send";

  private static final String FACILITY_ID_PARAM = "facilityId";
  private static final String PROGRAM_ID_PARAM = "programId";
  private static final String DIRECTION_PARAM = "direction";

  @MockBean
  private TransferPartnerService transferPartnerService;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private OrderService orderService;

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private FacilityReferenceDataService facilityService;

  @MockBean
  private ProgramReferenceDataService programService;

  private final UserDto user = new UserDataBuilder().withId(INITIAL_USER_ID).build();

  private FacilityDto requesting;
  private FacilityDto supplying;
  private ProgramDto program;
  private Order transferRequest;
  private OrderDto transferRequestDto;

  @Before
  public void setUp() {
    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    program = new ProgramDataBuilder().build();
    requesting = new FacilityDataBuilder().withName("Requesting")
        .withSupportedPrograms(Collections.singletonList(program)).build();
    supplying = new FacilityDataBuilder().withName("Supplying")
        .withSupportedPrograms(Collections.singletonList(program)).build();

    when(facilityService.findOne(requesting.getId())).thenReturn(requesting);
    when(facilityService.findOne(supplying.getId())).thenReturn(supplying);
    when(facilityService.findByIds(anySet())).thenReturn(Arrays.asList(requesting, supplying));
    when(programService.findOne(program.getId())).thenReturn(program);

    transferRequest = new OrderDataBuilder()
        .withType(OrderType.TRANSFER_REQUEST)
        .withExternalId(null)
        .withStatus(OrderStatus.CREATING)
        .withQuotedCost(BigDecimal.ZERO)
        .withProcessingPeriodId(null)
        .withProgramId(program.getId())
        .withFacilityId(requesting.getId())
        .withRequestingFacilityId(requesting.getId())
        .withReceivingFacilityId(requesting.getId())
        .withSupplyingFacilityId(supplying.getId())
        .withCreatedById(INITIAL_USER_ID)
        .withoutLineItems()
        .build();
    transferRequestDto = OrderDto.newInstance(transferRequest, exporter);

    given(orderRepository.findById(transferRequest.getId()))
        .willReturn(Optional.of(transferRequest));
    given(transferPartnerService
        .isValidPair(supplying.getId(), requesting.getId(), program.getId()))
        .willReturn(true);
  }

  @Test
  public void shouldReturnTransferPartners() {
    given(transferPartnerService
        .getPartners(requesting.getId(), program.getId(), TransferDirection.REQUEST))
        .willReturn(Collections.singletonList(supplying));

    FacilityDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID_PARAM, requesting.getId())
        .queryParam(PROGRAM_ID_PARAM, program.getId())
        .queryParam(DIRECTION_PARAM, TransferDirection.REQUEST)
        .when()
        .get(PARTNERS_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityDto[].class);

    assertThat(response.length, is(1));
    assertThat(response[0].getId(), is(supplying.getId()));
    verify(permissionService)
        .canViewTransferPartners(requesting.getId(), TransferDirection.REQUEST);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectTransferPartnersWhenUserHasNoRight() {
    doThrow(new MissingPermissionException(ORDER_CREATE))
        .when(permissionService)
        .canViewTransferPartners(requesting.getId(), TransferDirection.REQUEST);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID_PARAM, requesting.getId())
        .queryParam(PROGRAM_ID_PARAM, program.getId())
        .queryParam(DIRECTION_PARAM, TransferDirection.REQUEST)
        .when()
        .get(PARTNERS_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(equalTo(PERMISSION_MISSING)));
    verify(transferPartnerService, never()).getPartners(any(), any(), any());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectTransferPartnersWithUnknownDirection() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY_ID_PARAM, requesting.getId())
        .queryParam(PROGRAM_ID_PARAM, program.getId())
        .queryParam(DIRECTION_PARAM, "SIDEWAYS")
        .when()
        .get(PARTNERS_URL)
        .then()
        .statusCode(400);

    verify(transferPartnerService, never()).getPartners(any(), any(), any());
  }

  @Test
  public void shouldCreateTransferRequest() {
    given(orderService.createTransferRequest(any(OrderDto.class), eq(user.getId())))
        .willReturn(transferRequest);
    // the receiving facility is always the requesting one, whatever the client sends
    transferRequestDto.setReceivingFacility(supplying);
    transferRequestDto.setFacility(supplying);
    transferRequestDto.setQuotedCost(BigDecimal.TEN);
    transferRequestDto.setEmergency(true);
    // the creator is always the caller, whatever the client sends
    transferRequestDto.setCreatedBy(new UserDataBuilder().build());

    String type = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferRequestDto)
        .when()
        .post(REQUESTS_URL)
        .then()
        .statusCode(201)
        .extract().path("type");

    assertThat(type, is(OrderType.TRANSFER_REQUEST.name()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(permissionService).canCreateTransferRequest(requesting.getId());
    verify(transferPartnerService)
        .isValidPair(supplying.getId(), requesting.getId(), program.getId());

    ArgumentCaptor<OrderDto> captor = ArgumentCaptor.forClass(OrderDto.class);
    verify(orderService).createTransferRequest(captor.capture(), eq(user.getId()));
    OrderDto created = captor.getValue();
    assertThat(created.getId(), is((UUID) null));
    assertThat(created.getExternalId(), is((UUID) null));
    assertThat(created.getReceivingFacility().getId(), is(requesting.getId()));
    assertThat(created.getFacility().getId(), is(requesting.getId()));
    assertThat(created.getQuotedCost(), is(BigDecimal.ZERO));
    assertThat(created.getEmergency(), is(Boolean.FALSE));
    assertThat(created.getCreatedBy().getId(), is(user.getId()));
  }

  @Test
  public void shouldRejectTransferRequestForInvalidPair() {
    given(transferPartnerService
        .isValidPair(supplying.getId(), requesting.getId(), program.getId()))
        .willReturn(false);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferRequestDto)
        .when()
        .post(REQUESTS_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_INVALID_PAIR));
    verify(orderService, never()).createTransferRequest(any(), any());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectTransferRequestWithoutSupplyingFacility() {
    transferRequestDto.setSupplyingFacility(null);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferRequestDto)
        .when()
        .post(REQUESTS_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_REQUIRED_FIELD));
    verify(orderService, never()).createTransferRequest(any(), any());
    // the request itself breaks the RAML schema on purpose, so no RAML assertion here
  }

  @Test
  public void shouldRejectTransferRequestWhenUserHasNoRight() {
    doThrow(new MissingPermissionException(ORDER_CREATE))
        .when(permissionService).canCreateTransferRequest(requesting.getId());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferRequestDto)
        .when()
        .post(REQUESTS_URL)
        .then()
        .statusCode(403);

    verify(transferPartnerService, never()).isValidPair(any(), any(), any());
    verify(orderService, never()).createTransferRequest(any(), any());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateTransferRequest() {
    given(orderService.updateOrder(eq(transferRequest.getId()), any(OrderDto.class),
        eq(user.getId()))).willReturn(transferRequest);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(REQUEST_URL)
        .then()
        .statusCode(200);

    verify(permissionService).canCreateTransferRequest(requesting.getId());
    verify(transferPartnerService)
        .isValidPair(supplying.getId(), requesting.getId(), program.getId());
    verify(orderService)
        .updateOrder(eq(transferRequest.getId()), any(OrderDto.class), eq(user.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderThatIsNotTransferRequest() {
    transferRequest.setType(OrderType.REQUISITION_LESS);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(REQUEST_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_NOT_TRANSFER_REQUEST));
    verify(orderService, never()).updateOrder(any(), any(), any());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateTransferRequestWhenPairIsNoLongerValid() {
    given(transferPartnerService
        .isValidPair(supplying.getId(), requesting.getId(), program.getId()))
        .willReturn(false);

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(REQUEST_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_INVALID_PAIR));
    verify(orderService, never()).updateOrder(any(), any(), any());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWhenTransferRequestDoesNotExist() {
    UUID missingId = UUID.randomUUID();
    given(orderRepository.findById(missingId)).willReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, missingId)
        .body(transferRequestDto)
        .when()
        .put(REQUEST_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSendTransferRequest() {
    transferRequestDto.setOrderLineItems(Collections.singletonList(lineItem(10L)));
    given(orderService.updateOrder(eq(transferRequest.getId()), any(OrderDto.class),
        eq(user.getId()))).willReturn(transferRequest);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(SEND_URL)
        .then()
        .statusCode(200);

    verify(orderService).convertOrderedQuantitiesToPacks(transferRequest);
    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus(), is(OrderStatus.ORDERED));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSendTransferRequestWhenPairIsNoLongerValid() {
    given(transferPartnerService
        .isValidPair(supplying.getId(), requesting.getId(), program.getId()))
        .willReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(SEND_URL)
        .then()
        .statusCode(400);

    verify(orderRepository, never()).save(any(Order.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSendTransferRequestWithoutLineItems() {
    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(SEND_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_NOTHING_TO_SEND));
    verify(orderService, never()).updateOrder(any(), any(), any());
    verify(orderRepository, never()).save(any(Order.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSendTransferRequestWhenAllQuantitiesAreZero() {
    transferRequestDto.setOrderLineItems(Arrays.asList(lineItem(0L), lineItem(0L)));

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(SEND_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(TRANSFER_NOTHING_TO_SEND));
    verify(orderRepository, never()).save(any(Order.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateTransferRequestWithoutLineItems() {
    // saving a draft with no products is fine, only sending it is not
    given(orderService.updateOrder(eq(transferRequest.getId()), any(OrderDto.class),
        eq(user.getId()))).willReturn(transferRequest);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, transferRequest.getId())
        .body(transferRequestDto)
        .when()
        .put(REQUEST_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private OrderLineItemDto lineItem(Long orderedQuantity) {
    // shaped like the line items GET /orders/{id} returns, so the RAML check accepts them
    OrderLineItemDto lineItem = new OrderLineItemDto();
    lineItem.setId(UUID.randomUUID());
    lineItem.setOrderable(new OrderableDataBuilder().build());
    lineItem.setOrderedQuantity(orderedQuantity);
    lineItem.setTotalDispensingUnits(orderedQuantity);
    return lineItem;
  }
}
