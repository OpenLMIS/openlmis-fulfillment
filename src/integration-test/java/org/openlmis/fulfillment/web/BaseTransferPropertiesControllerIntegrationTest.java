package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

public abstract class BaseTransferPropertiesControllerIntegrationTest<T extends TransferProperties>
    extends BaseWebIntegrationTest {
  static final String ACCESS_TOKEN = "access_token";
  static final String RESOURCE_URL = "/api/transferProperties";
  static final String ID_URL = RESOURCE_URL + "/{id}";
  static final String SEARCH = RESOURCE_URL + "/search";
  static final String FACILITY = "facility";

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
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(TransferPropertiesFactory.newInstance(properties))
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

    when(transferPropertiesRepository.findOne(oldProperties.getId())).thenReturn(oldProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());

    // when
    TransferPropertiesDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(TransferPropertiesFactory.newInstance(newProperties))
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
  public void shouldDeleteProperties() {
    // given
    T properties = generateProperties();
    when(transferPropertiesRepository.findOne(properties.getId())).thenReturn(properties);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    when(transferPropertiesRepository.findOne(properties.getId())).thenReturn(null);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
  public void shouldGetChosenProperties() {
    // given
    T properties = generateProperties();
    when(transferPropertiesRepository.findOne(properties.getId())).thenReturn(properties);

    // when
    TransferPropertiesDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    when(transferPropertiesRepository.findOne(properties.getId())).thenReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    when(transferPropertiesRepository.findFirstByFacilityId(properties.getFacilityId()))
        .thenReturn(properties);

    TransferPropertiesDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
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
    when(transferPropertiesRepository.findFirstByFacilityId(any(UUID.class)))
        .thenReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(FACILITY, UUID.randomUUID())
        .when()
        .get(SEARCH)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  abstract T generateProperties();

  abstract void assertTransferProperties(T actual);

}
