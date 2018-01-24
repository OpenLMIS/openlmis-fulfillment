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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentDraftRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.util.PageImplRepresentation;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.openlmis.fulfillment.web.shipment.ShipmentDtoDataBuilder;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ShipmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/shipments";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";

  @Value("${service.url}")
  private String serviceUrl;

  @MockBean(answer = Answers.RETURNS_MOCKS)
  protected ShipmentRepository shipmentRepository;

  @MockBean(answer = Answers.RETURNS_MOCKS)
  protected ShipmentDraftRepository shipmentDraftRepository;

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private DateHelper dateHelper;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private StockEventStockManagementService stockEventService;

  @MockBean
  private StockEventBuilder stockEventBuilder;

  @Mock
  private Order order;

  @Captor
  private ArgumentCaptor<Shipment> captor;

  private ShipmentDto shipmentDto;
  private ShipmentDto shipmentDtoExpected;
  private UUID userId = UUID.randomUUID();
  private Shipment shipment;
  private UUID orderId = UUID.randomUUID();

  @Before
  public void setUp() {
    when(dateHelper.getCurrentDateTimeWithSystemZone())
        .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")));
    when(authenticationHelper.getCurrentUser()).thenReturn(new UserDto(userId));

    generateShipment();

    when(shipmentRepository.findOne(shipmentDtoExpected.getId())).thenReturn(shipment);
    when(shipmentRepository.save(any(Shipment.class))).thenAnswer(new SaveAnswer<>());
    when(orderRepository.findOne(shipmentDtoExpected.getOrder().getId())).thenReturn(order);
    when(orderRepository.save(any(Order.class))).thenAnswer(new SaveAnswer<>());

    when(order.canBeFulfilled()).thenReturn(true);
  }

  private void generateShipment() {
    shipmentDtoExpected = new ShipmentDto();
    shipmentDtoExpected.setServiceUrl(serviceUrl);
    ShipmentLineItem lineItem = new ShipmentLineItemDataBuilder().withoutId().build();
    shipment = generateShipment(lineItem);
    List<ShipmentLineItemDto> lineItemsDtos = exportToDto(lineItem);
    shipmentDtoExpected.setLineItems(lineItemsDtos);

    shipmentDto = new ShipmentDtoDataBuilder()
        .withoutShippedBy()
        .withoutShippedDate()
        .withOrder(new ObjectReferenceDto(shipmentDtoExpected.getOrder().getId()))
        .withNotes(shipmentDtoExpected.getNotes())
        .withLineItems(lineItemsDtos)
        .build();
  }

  private Shipment generateShipment(ShipmentLineItem lineItem) {
    return new ShipmentDataBuilder()
        .withShipDetails(new CreationDetailsDataBuilder()
            .withUserId(userId)
            .withDate(dateHelper.getCurrentDateTimeWithSystemZone())
            .build())
        .withOrder(new Order(orderId))
        .withLineItems(singletonList(lineItem))
        .build();
  }

  private List<ShipmentLineItemDto> exportToDto(ShipmentLineItem lineItem) {
    shipment.export(shipmentDtoExpected);
    ShipmentLineItemDto lineItemDto = new ShipmentLineItemDto();
    lineItemDto.setServiceUrl(serviceUrl);
    lineItem.export(lineItemDto);

    return singletonList(lineItemDto);
  }

  @Test
  public void shouldCreateShipment() {
    Order shipmentOrder = shipment.getOrder();
    shipmentOrder.setStatus(OrderStatus.ORDERED);
    when(orderRepository.findOne(shipment.getOrder().getId())).thenReturn(shipment.getOrder());
    //necessary as SaveAnswer change shipment id value also in captor
    when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

    ShipmentDto extracted = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ShipmentDto.class);

    shipmentOrder.setStatus(OrderStatus.SHIPPED);
    shipmentOrder.setUpdateDetails(new UpdateDetails(INITIAL_USER_ID,
        ZonedDateTime.of(2015, 5, 7, 10, 5, 20, 500, ZoneId.systemDefault())));

    assertEquals(shipmentDtoExpected, extracted);
    verify(orderRepository).save(shipmentOrder);
    verify(shipmentRepository).save(captor.capture());
    verify(stockEventBuilder).fromShipment(any(Shipment.class));
    verify(stockEventService).submit(any(StockEventDto.class));
    assertTrue(reflectionEquals(shipment, captor.getValue(), singletonList("id")));
    assertNull(captor.getValue().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnBadRequestIfShipmentOrderIsNotGiven() {
    shipmentDto.setOrder((ObjectReferenceDto) null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDERLESS_NOT_SUPPORTED));

    verify(shipmentRepository, never()).save(any(Shipment.class));
    verify(stockEventBuilder, never()).fromShipment(any(Shipment.class));
    verify(stockEventService, never()).submit(any(StockEventDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnBadRequestIfShipmentOrderHasInvalidStatus() {
    when(order.canBeFulfilled()).thenReturn(false);
    when(order.getStatus()).thenReturn(OrderStatus.IN_ROUTE);


    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDER_STATUS_INVALID));

    verify(shipmentRepository, never()).save(any(Shipment.class));
    verify(stockEventBuilder, never()).fromShipment(any(Shipment.class));
    verify(stockEventService, never()).submit(any(StockEventDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }


  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToEditShipments() {
    doThrow(new MissingPermissionException("test"))
        .when(permissionService).canEditShipment(any(ShipmentDto.class));

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.ERROR_PERMISSION_MISSING));

    verify(shipmentRepository, never()).save(any(Shipment.class));
    verify(stockEventBuilder, never()).fromShipment(any(Shipment.class));
    verify(stockEventService, never()).submit(any(StockEventDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldFindShipmentBasedOnOrder() {
    when(orderRepository.findOne(orderId))
        .thenReturn(shipment.getOrder());
    when(shipmentRepository.findByOrder(eq(shipment.getOrder()), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(shipment)));

    PageImplRepresentation response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 2)
        .queryParam("size", 10)
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, orderId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(PageImplRepresentation.class);

    assertEquals(10, response.getSize());
    assertEquals(2, response.getNumber());
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(21, response.getTotalElements());
    assertEquals(3, response.getTotalPages());
    assertEquals(shipmentDtoExpected, getPageContent(response, ShipmentDto.class).get(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderIsNotFoundWhenGetShipments() {
    when(orderRepository.findOne(orderId)).thenReturn(null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, orderId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.ERROR_ORDER_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderIsNotGivenWhenGetShipments() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDER_REQUIRED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToGetShipments() {
    when(orderRepository.findOne(orderId))
        .thenReturn(shipment.getOrder());
    doThrow(new MissingPermissionException("test"))
        .when(permissionService).canViewShipment(shipment.getOrder());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, orderId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.ERROR_PERMISSION_MISSING));

    verify(shipmentDraftRepository, never()).findByOrder(any(Order.class), any(Pageable.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetShipment() {
    ShipmentDto extracted = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, shipmentDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ShipmentDto.class);

    assertEquals(shipmentDtoExpected, extracted);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfShipmentIsNotFound() {
    when(shipmentRepository.findOne(shipmentDtoExpected.getId())).thenReturn(null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, shipmentDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(404)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_NOT_FOUND));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnForbiddenIfUserHasNoRightsToGetShipment() {
    doThrow(new MissingPermissionException("test"))
        .when(permissionService).canViewShipment(shipment);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .pathParam(ID, shipmentDtoExpected.getId())
        .when()
        .get(ID_RESOURCE_URL)
        .then()
        .statusCode(403)
        .body(MESSAGE_KEY, equalTo(MessageKeys.ERROR_PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }
}
