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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentLineItemDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.openlmis.fulfillment.web.shipment.ShipmentDtoDataBuilder;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ShipmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/shipments";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";

  @Value("${service.url}")
  private String serviceUrl;

  @MockBean(answer = Answers.RETURNS_MOCKS)
  protected ShipmentRepository shipmentRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private DateHelper dateHelper;

  @MockBean
  private PermissionService permissionService;

  private ShipmentDto shipmentDto;
  private ShipmentDto shipmentDtoExpected;
  private UUID userId = UUID.randomUUID();
  private Shipment shipment;

  @Before
  public void setUp() {
    when(dateHelper.getCurrentDateTimeWithSystemZone())
        .thenReturn(ZonedDateTime.now(ZoneId.of("UTC")));
    when(authenticationHelper.getCurrentUser()).thenReturn(new UserDto(userId));

    generateShipment();

    when(shipmentRepository.findOne(shipmentDtoExpected.getId())).thenReturn(shipment);
    when(shipmentRepository.save(any(Shipment.class))).thenAnswer(new SaveAnswer<>());
  }

  private void generateShipment() {
    shipmentDtoExpected = new ShipmentDto();
    shipmentDtoExpected.setServiceUrl(serviceUrl);
    ShipmentLineItem lineItem = new ShipmentLineItemDataBuilder().build();
    shipment = generateShipment(lineItem);
    List<ShipmentLineItemDto> lineItemsDtos = exportToDto(lineItem);
    shipmentDtoExpected.setLineItems(lineItemsDtos);

    shipmentDto = new ShipmentDtoDataBuilder()
        .withoutId()
        .withoutShippedBy()
        .withoutShippedDate()
        .withOrder(new ObjectReferenceDto(shipmentDtoExpected.getOrder().getId()))
        .withNotes(shipmentDtoExpected.getNotes())
        .withLineItems(lineItemsDtos)
        .build();
  }

  private Shipment generateShipment(ShipmentLineItem lineItem) {
    return new ShipmentDataBuilder()
        .withId(SAVE_ANSWER_ID)
        .withShipDetails(new CreationDetailsDataBuilder()
            .withUserId(userId)
            .withDate(dateHelper.getCurrentDateTimeWithSystemZone())
            .build())
        .withOrder(new Order(UUID.randomUUID()))
        .withLineItems(Collections.singletonList(lineItem))
        .build();
  }

  private List<ShipmentLineItemDto> exportToDto(ShipmentLineItem lineItem) {
    shipment.export(shipmentDtoExpected);
    ShipmentLineItemDto lineItemDto = new ShipmentLineItemDto();
    lineItemDto.setServiceUrl(serviceUrl);
    lineItem.export(lineItemDto);

    return Collections.singletonList(lineItemDto);
  }

  @Test
  public void shouldCreateShipment() {
    ShipmentDto extracted = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(shipmentDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201)
        .extract().as(ShipmentDto.class);

    verify(shipmentRepository).save(refEq(shipment));
    assertEquals(shipmentDtoExpected, extracted);
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
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
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
