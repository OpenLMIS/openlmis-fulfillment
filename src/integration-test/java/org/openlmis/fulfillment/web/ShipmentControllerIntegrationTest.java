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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ShipmentControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/shipments";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";

  @Value("${service.url}")
  private String serviceUrl;

  @MockBean
  private ShipmentRepository shipmentRepository;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private DateHelper dateHelper;

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
    shipment = new ShipmentDataBuilder()
        .withId(SAVE_ANSWER_ID)
        .withShipDetails(new CreationDetailsDataBuilder()
            .withUserId(userId)
            .withDate(dateHelper.getCurrentDateTimeWithSystemZone())
            .build())
        .withOrder(new Order(UUID.randomUUID()))
        .build();
    shipment.export(shipmentDtoExpected);

    shipmentDto = new ShipmentDto(null, null,
        new ObjectReferenceDto(shipmentDtoExpected.getOrder().getId()), null, null,
        shipmentDtoExpected.getNotes());
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
}
