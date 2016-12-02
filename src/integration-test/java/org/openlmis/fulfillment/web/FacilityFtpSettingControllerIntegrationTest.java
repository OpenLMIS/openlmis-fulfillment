package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.UUID;

import guru.nidi.ramltester.junit.RamlMatchers;

public class FacilityFtpSettingControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilityFtpSettings";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String SEARCH_URL = RESOURCE_URL + "/search";
  private static final String SEARCH_FACILITY_PARAM = "facility";

  @MockBean
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Test
  public void shouldCreateSetting() {
    // given
    FacilityFtpSetting setting = generateSetting();
    given(facilityFtpSettingRepository.save(any(FacilityFtpSetting.class)))
        .willAnswer(new SaveAnswer<Order>());

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(setting)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(201);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateSetting() {
    // given
    FacilityFtpSetting newSetting = generateSetting();
    FacilityFtpSetting oldSetting = generateSetting();

    when(facilityFtpSettingRepository.findOne(oldSetting.getId())).thenReturn(oldSetting);
    given(facilityFtpSettingRepository.save(any(FacilityFtpSetting.class)))
        .willAnswer(new SaveAnswer<FacilityFtpSetting>());

    // when
    FacilityFtpSetting response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldSetting.getId())
        .body(newSetting)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityFtpSetting.class);

    // then
    assertEquals(response.getId(), oldSetting.getId());
    assertEquals(response.getFacilityId(), newSetting.getFacilityId());
    assertEquals(response.getServerHost(), newSetting.getServerHost());
    assertEquals(response.getServerPort(), newSetting.getServerPort());
    assertEquals(response.getPath(), newSetting.getPath());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteSetting() {
    // given
    FacilityFtpSetting setting = generateSetting();
    when(facilityFtpSettingRepository.findOne(setting.getId())).thenReturn(setting);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", setting.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentSettings() {
    // given
    FacilityFtpSetting setting = generateSetting();
    when(facilityFtpSettingRepository.findOne(setting.getId())).thenReturn(null);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", setting.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenSetting() {
    // given
    FacilityFtpSetting setting = generateSetting();
    when(facilityFtpSettingRepository.findOne(setting.getId())).thenReturn(setting);

    // when
    FacilityFtpSetting response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", setting.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityFtpSetting.class);

    // then
    assertEquals(response.getId(), setting.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldFindBySupplyingFacility() {
    // given
    FacilityFtpSetting setting = generateSetting();
    when(facilityFtpSettingRepository.searchFacilityFtpSettings(setting.getFacilityId()))
        .thenReturn(Lists.newArrayList(setting));

    // when
    FacilityFtpSetting[] response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .queryParam(SEARCH_FACILITY_PARAM, setting.getFacilityId())
        .when()
        .get(SEARCH_URL)
        .then()
        .statusCode(200)
        .extract().as(FacilityFtpSetting[].class);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(response.length, 1);

    for (FacilityFtpSetting responseSetting : response) {
      assertEquals(
          responseSetting.getFacilityId(),
          setting.getFacilityId());
    }
  }

  @Test
  public void shouldNotGetNonexistentSetting() {
    // given
    FacilityFtpSetting setting = generateSetting();
    when(facilityFtpSettingRepository.findOne(setting.getId())).thenReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", setting.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private FacilityFtpSetting generateSetting() {
    FacilityFtpSetting setting = new FacilityFtpSetting();
    setting.setId(UUID.randomUUID());
    setting.setFacilityId(UUID.randomUUID());
    setting.setServerHost("host");
    setting.setServerPort("9000");
    setting.setPath("path");
    setting.setUsername("username");
    setting.setPassword("password");
    return setting;
  }
}
