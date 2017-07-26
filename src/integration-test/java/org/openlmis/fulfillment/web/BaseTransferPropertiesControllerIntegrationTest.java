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

import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import guru.nidi.ramltester.junit.RamlMatchers;
import org.junit.Test;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public abstract class BaseTransferPropertiesControllerIntegrationTest<T extends TransferProperties>
    extends BaseWebIntegrationTest {
  static final String RESOURCE_URL = "/api/transferProperties";
  static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH = RESOURCE_URL + "/search";
  private static final String FACILITY = "facility";

  @MockBean
  TransferPropertiesRepository transferPropertiesRepository;

  @Test
  public void shouldCreateProperties() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(toDto(properties))
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateProperties() {
    // given
    T oldProperties = generateProperties();
    T newProperties = generateProperties();
    newProperties.setId(oldProperties.getId());
    newProperties.setFacilityId(oldProperties.getFacilityId());

    given(transferPropertiesRepository.findOne(newProperties.getId())).willReturn(oldProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());

    // when
    TransferPropertiesDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(toDto(newProperties))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    // then
    assertTransferProperties((T) TransferPropertiesFactory.newInstance(response));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateNonexistentProperties() {
    // given
    T oldProperties = generateProperties();
    T newProperties = generateProperties();
    newProperties.setId(oldProperties.getId());
    newProperties.setFacilityId(oldProperties.getFacilityId());

    given(transferPropertiesRepository.findOne(newProperties.getId())).willReturn(null);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(toDto(newProperties))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(404);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGet400IfPropertiesHasDifferentFacilityIdWhenUpdate() {
    // given
    T oldProperties = generateProperties();
    T newProperties = generateProperties();
    T otherProperties = generateProperties();
    newProperties.setId(oldProperties.getId());
    newProperties.setFacilityId(oldProperties.getFacilityId());

    given(transferPropertiesRepository.findOne(newProperties.getId())).willReturn(otherProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());

    // when
    TransferPropertiesDto object = toDto(newProperties);
    object.getFacility().setId(UUID.randomUUID());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(object)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteProperties() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.findOne(properties.getId())).willReturn(properties);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentProperties() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.findOne(properties.getId())).willReturn(null);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeletePropertiesWithConflicts() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.findOne(properties.getId())).willThrow(
        new DataIntegrityViolationException("This exception is required by IT"));

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(409);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenProperties() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.findOne(properties.getId())).willReturn(properties);

    // when
    TransferPropertiesDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    // then
    assertTransferProperties((T) TransferPropertiesFactory.newInstance(response));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentProperties() {
    // given
    T properties = generateProperties();
    given(transferPropertiesRepository.findOne(properties.getId())).willReturn(null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindPropertiesByFacilityId() {
    T properties = generateProperties();
    given(transferPropertiesRepository.findFirstByFacilityId(properties.getFacilityId()))
        .willReturn(properties);

    TransferPropertiesDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY, properties.getFacilityId())
        .when()
        .get(SEARCH)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    assertTransferProperties((T) TransferPropertiesFactory.newInstance(response));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnEmptyListIfPropertiesCannotBeFound() {
    given(transferPropertiesRepository.findFirstByFacilityId(any(UUID.class)))
        .willReturn(null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY, UUID.randomUUID())
        .when()
        .get(SEARCH)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToViewTransferProperties() {
    denyUserAllRights();
    T properties = generateProperties();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToCreateOrderFileTemplate() {
    denyUserAllRights();
    T properties = generateProperties();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(toDto(properties))
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateOrderFileTemplate() {
    denyUserAllRights();
    T oldProperties = generateProperties();
    T newProperties = generateProperties();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(toDto(newProperties))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToDeleteOrderFileTemplate() {
    denyUserAllRights();
    T properties = generateProperties();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", properties.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToSearchTransferProperties() {
    denyUserAllRights();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .queryParam(FACILITY, UUID.randomUUID())
        .when()
        .get(SEARCH)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  abstract T generateProperties();

  abstract void assertTransferProperties(T actual);

  abstract TransferPropertiesDto toDto(T properties);

}
