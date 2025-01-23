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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentDraftRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.PageDto;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.openlmis.fulfillment.web.shipment.ShipmentDtoDataBuilder;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.OrderObjectReferenceDto;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.openlmis.fulfillment.web.util.VersionIdentityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings({"PMD.TooManyMethods"})
public class ShipmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/shipments";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";

  @Value("${service.url}")
  private String serviceUrl;

  @MockBean
  protected ShipmentRepository shipmentRepository;

  @MockBean
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

  @Autowired
  private ShipmentService shipmentService;

  private EntityManager entityManager;

  @SpyBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private Order order;

  @Captor
  private ArgumentCaptor<BaseEntity> captor;

  private ShipmentDto shipmentDto;
  private ShipmentDto shipmentDtoExpected;
  private UUID userId = UUID.randomUUID();
  private Shipment shipment;
  private UUID orderId = UUID.randomUUID();
  private List<OrderableDto> orderables;
  private OrderableDto orderableDto;

  @Before
  public void setUp() {
    when(dateHelper.getCurrentDateTimeWithSystemZone())
        .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")));
    when(authenticationHelper.getCurrentUser()).thenReturn(new UserDto(userId));

    generateShipment();

    when(shipmentRepository.findById(shipmentDtoExpected.getId()))
        .thenReturn(Optional.of(shipment));
    when(orderRepository.findById(shipmentDtoExpected.getOrder().getId()))
        .thenReturn(Optional.of(order));

    when(order.canBeFulfilled()).thenReturn(true);

    entityManager = Mockito.mock(EntityManager.class);
    ReflectionTestUtils.setField(shipmentService, "entityManager", entityManager);
  }

  private void generateShipment() {
    shipmentDtoExpected = new ShipmentDto();
    shipmentDtoExpected.setServiceUrl(serviceUrl);

    ShipmentLineItem lineItem = new ShipmentLineItemDataBuilder()
        .withoutId()
        .withOrderable(UUID.randomUUID(), 1L)
        .build();
    shipment = generateShipment(lineItem);

    orderables = shipment
        .getLineItems()
        .stream()
        .map(line -> new OrderableDataBuilder()
            .withId(line.getOrderable().getId())
            .withVersionNumber(line.getOrderable().getVersionNumber())
            .build())
        .collect(Collectors.toList());

    given(orderableReferenceDataService.findByIdentities(anySetOf(VersionEntityReference.class)))
        .willReturn(orderables);

    orderableDto = findOrderable(orderables, lineItem);
    List<ShipmentLineItemDto> lineItemsDtos = exportToDto(lineItem, orderableDto);
    shipmentDtoExpected.setLineItems(lineItemsDtos);

    shipmentDto = new ShipmentDtoDataBuilder()
        .withoutShippedBy()
        .withoutShippedDate()
        .withOrder(new OrderObjectReferenceDto(shipmentDtoExpected.getOrder().getId()))
        .withNotes(shipmentDtoExpected.getNotes())
        .withLineItems(shipmentDtoExpected.lineItems())
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

  private OrderableDto findOrderable(List<OrderableDto> orderables, ShipmentLineItem line) {
    return orderables
        .stream()
        .filter(orderable -> orderable.getIdentity()
            .equals(new VersionIdentityDto(line.getOrderable())))
        .findFirst()
        .orElse(null);
  }

  private List<ShipmentLineItemDto> exportToDto(ShipmentLineItem lineItem, OrderableDto orderable) {
    shipment.export(shipmentDtoExpected);
    ShipmentLineItemDto lineItemDto = new ShipmentLineItemDto();
    lineItemDto.setServiceUrl(serviceUrl);
    lineItem.export(lineItemDto, orderable);

    return singletonList(lineItemDto);
  }

  @Test
  public void shouldCreateShipment() {
    shipmentDtoExpected.setId(null);

    Order shipmentOrder = shipment.getOrder();
    shipmentOrder.setStatus(OrderStatus.ORDERED);
    when(orderRepository.findById(shipment.getOrder().getId()))
        .thenReturn(Optional.of(shipment.getOrder()));
    //necessary as SaveAnswer change shipment id value also in captor
    when(stockEventBuilder.fromShipment(any(Shipment.class))).thenReturn(
        Optional.of(new StockEventDto()));

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
    verify(entityManager, times(2)).persist(captor.capture());
    verify(stockEventBuilder).fromShipment(any(Shipment.class));
    verify(stockEventService).submit(any(StockEventDto.class));
    assertTrue(reflectionEquals(shipment, captor.getAllValues().iterator().next(),
        singletonList("id")));
    assertNull(captor.getAllValues().iterator().next().getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldReturnBadRequestIfShipmentOrderIsNotGiven() {
    shipmentDto.setOrder((OrderObjectReferenceDto) null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.SHIPMENT_ORDERLESS_NOT_SUPPORTED));

    verify(entityManager, never()).persist(any(Shipment.class));
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

    verify(entityManager, never()).persist(any(Shipment.class));
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
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    verify(entityManager, never()).persist(any(Shipment.class));
    verify(stockEventBuilder, never()).fromShipment(any(Shipment.class));
    verify(stockEventService, never()).submit(any(StockEventDto.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldFindShipmentBasedOnOrder() {
    when(orderRepository.findById(orderId))
        .thenReturn(Optional.of(shipment.getOrder()));
    when(shipmentRepository.findByOrder(eq(shipment.getOrder()), any(Pageable.class)))
        .thenReturn(new PageImpl<>(singletonList(shipment)));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam("page", 2)
        .queryParam("size", 10)
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, orderId)
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
    assertEquals(shipmentDtoExpected, getPageContent(response, ShipmentDto.class).get(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestIfOrderIsNotFoundWhenGetShipments() {
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam(ORDER_ID, orderId)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(400)
        .body(MESSAGE_KEY, equalTo(MessageKeys.ORDER_NOT_FOUND));

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
    when(orderRepository.findById(orderId))
        .thenReturn(Optional.of(shipment.getOrder()));
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
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

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
    when(shipmentRepository.findById(shipmentDtoExpected.getId())).thenReturn(Optional.empty());

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
        .body(MESSAGE_KEY, equalTo(MessageKeys.PERMISSION_MISSING));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }
}
