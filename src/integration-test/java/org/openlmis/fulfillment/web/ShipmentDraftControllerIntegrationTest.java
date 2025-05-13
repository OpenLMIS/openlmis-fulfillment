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

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.builder.EqualsBuilder.reflectionEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySetOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ShipmentDraft;
import org.openlmis.fulfillment.domain.ShipmentDraftLineItem;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentDraftRepository;
import org.openlmis.fulfillment.service.PageDto;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDraftDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDraftLineItemDataBuilder;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.shipmentdraft.ShipmentDraftDto;
import org.openlmis.fulfillment.web.shipmentdraft.ShipmentDraftDtoDataBuilder;
import org.openlmis.fulfillment.web.util.OrderObjectReferenceDto;
import org.openlmis.fulfillment.web.util.VersionIdentityDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ShipmentDraftControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/shipmentDrafts";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";

  private static final String PERMISSION_NAME = "test";

  @Value("${service.url}")
  private String serviceUrl;

  @MockBean
  protected ShipmentDraftRepository shipmentDraftRepository;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private OrderRepository orderRepository;

  @SpyBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private DateHelper dateHelper;

  @Captor
  private ArgumentCaptor<ShipmentDraft> captor;

  private ShipmentDraftLineItem lineItem;
  private ShipmentDraftDto shipmentDraftDto;
  private ShipmentDraftDto shipmentDraftDtoExpected;
  private ShipmentDraft shipmentDraft;
  private UUID draftIdFromUser = UUID.randomUUID();
  private UUID orderId = UUID.randomUUID();
  private ZonedDateTime modifiedDate =
      ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault());

  @Before
  public void setUp() {
    generateShipmentDraft();

    when(shipmentDraftRepository.findById(shipmentDraftDtoExpected.getId()))
        .thenReturn(Optional.of(shipmentDraft));
    when(shipmentDraftRepository.save(any(ShipmentDraft.class))).thenAnswer(new SaveAnswer<>());
  }

  private void generateShipmentDraft() {
    shipmentDraftDtoExpected = new ShipmentDraftDto();
    shipmentDraftDtoExpected.setServiceUrl(serviceUrl);

    lineItem = new ShipmentDraftLineItemDataBuilder()
        .withoutId()
        .withOrderable(UUID.randomUUID(), 1L)
        .build();
    shipmentDraft = generateShipmentDraft(lineItem);

    List<OrderableDto> orderables = shipmentDraft
        .viewLineItems()
        .stream()
        .map(line -> new OrderableDataBuilder()
            .withId(line.getOrderable().getId())
            .withVersionNumber(line.getOrderable().getVersionNumber())
            .build())
        .collect(Collectors.toList());

    given(orderableReferenceDataService.findByIdentities(anySetOf(VersionEntityReference.class)))
        .willReturn(orderables);

    OrderableDto orderableDto = findOrderable(orderables, lineItem);

    List<ShipmentLineItemDto> lineItemsDtos = exportToDto(lineItem, orderableDto);
    shipmentDraftDtoExpected.setLineItems(lineItemsDtos);

    shipmentDraftDto = new ShipmentDraftDtoDataBuilder()
        .withOrder(new OrderObjectReferenceDto(shipmentDraftDtoExpected.getOrder().getId()))
        .withNotes(shipmentDraftDtoExpected.getNotes())
        .withLineItems(lineItemsDtos)
        .build();
  }

  private ShipmentDraft generateShipmentDraft(ShipmentDraftLineItem lineItem) {
    return new ShipmentDraftDataBuilder()
        .withOrder(new Order(orderId))
        .withLineItems(singletonList(lineItem))
        .build();
  }

  private List<ShipmentLineItemDto> exportToDto(ShipmentDraftLineItem lineItem,
      OrderableDto orderableDto) {
    shipmentDraft.export(shipmentDraftDtoExpected);
    return exportShipmentLineItem(lineItem, orderableDto);
  }

  private List<ShipmentLineItemDto> exportShipmentLineItem(ShipmentDraftLineItem lineItem,
      OrderableDto orderableDto) {
    ShipmentLineItemDto lineItemDto = new ShipmentLineItemDto();
    lineItemDto.setServiceUrl(serviceUrl);

    lineItem.export(lineItemDto, orderableDto);

    return singletonList(lineItemDto);
  }

  private OrderableDto findOrderable(List<OrderableDto> orderables, ShipmentDraftLineItem line) {
    return orderables
        .stream()
        .filter(orderable -> orderable.getIdentity()
            .equals(new VersionIdentityDto(line.getOrderable())))
        .findFirst()
        .orElse(null);
  }

  @Test
  public void shouldCreateShipmentDraft() {
    //necessary as SaveAnswer change shipment id value also in captor
    when(shipmentDraftRepository.save(any(ShipmentDraft.class))).thenReturn(shipmentDraft);

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(modifiedDate);

    Order order = new OrderDataBuilder().withOrderedStatus().build();
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    ShipmentDraftDto extracted = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ShipmentDraftDto.class);

    order.setStatus(OrderStatus.FULFILLING);
    order.setUpdateDetails(new UpdateDetails(INITIAL_USER_ID, modifiedDate));

    assertEquals(shipmentDraftDtoExpected, extracted);
    verify(shipmentDraftRepository).save(captor.capture());
    verify(orderRepository).save(order);
    assertTrue(reflectionEquals(shipmentDraft, captor.getValue(), singletonList("id")));
    assertNull(captor.getValue().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderStatusIsNotOrdered() {
    Order order = new OrderDataBuilder().withFulfillingStatus().build();
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY,
            equalTo(MessageKeys.CANNOT_CREATE_SHIPMENT_DRAFT_FOR_ORDER_WITH_WRONG_STATUS));

    verify(shipmentDraftRepository, never()).save(any(ShipmentDraft.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnBadRequestIfShipmentDraftOrderIsNotGiven() {
    shipmentDraftDto.setOrder((OrderObjectReferenceDto) null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDERLESS_NOT_SUPPORTED));

    verify(shipmentDraftRepository, never()).save(any(ShipmentDraft.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToEditShipmentDrafts() {
    stubMissingPermission();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    verify(shipmentDraftRepository, never()).save(any(ShipmentDraft.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateShipmentDraftIfNotFoundById() {
    when(shipmentDraftRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
    shipmentDraftDto.setId(draftIdFromUser);

    ShipmentDraftDto extracted = restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .put(ID_RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ShipmentDraftDto.class);

    verifyAfterPut(extracted);
  }

  @Test
  public void shouldUpdateShipmentDraftIfFoundById() {
    ShipmentDraft existingDraft = new ShipmentDraftDataBuilder()
        .withId(draftIdFromUser)
        .withNotes("old notes")
        .build();

    when(shipmentDraftRepository.findById(any(UUID.class))).thenReturn(Optional.of(existingDraft));
    shipmentDraftDto.setId(draftIdFromUser);

    ShipmentDraftDto extracted = restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .put(ID_RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ShipmentDraftDto.class);

    verifyAfterPut(extracted);
  }

  @Test
  public void shouldReturnBadRequestIfIdMismatch() {
    shipmentDraftDto.setId(UUID.randomUUID());

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .put(ID_RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_DRAFT_ID_MISMATCH));

    verify(shipmentDraftRepository, never()).findById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfShipmentDraftOrderIsNotGivenWhenPut() {
    shipmentDraftDto.setOrder((OrderObjectReferenceDto) null);
    shipmentDraftDto.setId(draftIdFromUser);

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .put(ID_RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDERLESS_NOT_SUPPORTED));

    verify(shipmentDraftRepository, never()).findById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToEditShipmentDraftsWhenPut() {
    stubMissingPermission();
    shipmentDraftDto.setId(draftIdFromUser);

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDraftDto)
        .when()
        .put(ID_RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    verify(shipmentDraftRepository, never()).findById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetShipmentDraft() {
    ShipmentDraftDto extracted = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, shipmentDraftDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ShipmentDraftDto.class);

    assertEquals(shipmentDraftDtoExpected, extracted);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfShipmentDraftIsNotFound() {
    when(shipmentDraftRepository.findById(shipmentDraftDtoExpected.getId()))
        .thenReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, shipmentDraftDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(404)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToGetShipmentDraft() {
    doThrow(new MissingPermissionException("test"))
        .when(permissionService).canViewShipmentDraft(shipmentDraft);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, shipmentDraftDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindShipmentDraftBasedOnOrder() {
    when(orderRepository.findById(shipmentDraftDtoExpected.getOrder().getId()))
        .thenReturn(Optional.of(shipmentDraft.getOrder()));
    when(shipmentDraftRepository.findByOrder(eq(shipmentDraft.getOrder()), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(shipmentDraft)));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 2)
        .queryParam("size", 10)
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, shipmentDraftDtoExpected.getOrder().getId())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageDto.class);

    assertEquals(10, response.getSize());
    assertEquals(2, response.getNumber());
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(21, response.getTotalElements());
    assertEquals(3, response.getTotalPages());
    assertEquals(shipmentDraftDtoExpected, getPageContent(response, ShipmentDraftDto.class).get(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderIsNotFoundWhenGetShipmentDrafts() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam("orderId", shipmentDraftDtoExpected.getOrder().getId())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_DRAFT_ORDER_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderIsNotGivenWhenGetShipmentDrafts() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_DRAFT_ORDER_REQUIRED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToGetDrafts() {
    when(orderRepository.findById(orderId))
        .thenReturn(Optional.of(shipmentDraft.getOrder()));
    doThrow(new MissingPermissionException(PERMISSION_NAME))
        .when(permissionService).canViewShipmentDraft(shipmentDraft.getOrder());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam("orderId", orderId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    verify(shipmentDraftRepository, never()).findByOrder(any(Order.class), any(Pageable.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteShipmentDraft() {
    when(shipmentDraftRepository.findById(draftIdFromUser)).thenReturn(Optional.of(shipmentDraft));

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(modifiedDate);

    Order order = new OrderDataBuilder().build();
    when(orderRepository.findById(any())).thenReturn(Optional.of(order));

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .delete(ID_RESOURCE_URL)
        .then()
        .statusCode(204);

    order.setStatus(OrderStatus.ORDERED);
    order.setUpdateDetails(new UpdateDetails(INITIAL_USER_ID, modifiedDate));

    verify(shipmentDraftRepository).deleteById(draftIdFromUser);
    verify(orderRepository).save(order);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfShipmentDraftIsNotFoundWhenDelete() {
    when(shipmentDraftRepository.findById(draftIdFromUser)).thenReturn(Optional.empty());

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .delete(ID_RESOURCE_URL)
        .then()
        .statusCode(404)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_NOT_FOUND));

    verify(shipmentDraftRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToDeleteShipmentDraft() {
    when(shipmentDraftRepository.findById(draftIdFromUser))
        .thenReturn(Optional.of(shipmentDraft));
    doThrow(new MissingPermissionException(PERMISSION_NAME))
        .when(permissionService).canEditShipmentDraft(shipmentDraft);

    restAssured.given()
        .pathParam(ID, draftIdFromUser)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .delete(ID_RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    verify(shipmentDraftRepository, never()).deleteById(any(UUID.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }


  private void verifyAfterPut(ShipmentDraftDto extracted) {
    verify(shipmentDraftRepository).findById(draftIdFromUser);
    shipmentDraft.setId(draftIdFromUser);
    verify(shipmentDraftRepository).save(refEq(shipmentDraft));
    shipmentDraftDtoExpected.setId(draftIdFromUser);
    assertEquals(shipmentDraftDtoExpected, extracted);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void stubMissingPermission() {
    doThrow(new MissingPermissionException(PERMISSION_NAME))
        .when(permissionService).canEditShipmentDraft(any(ShipmentDraftDto.class));
  }
}
