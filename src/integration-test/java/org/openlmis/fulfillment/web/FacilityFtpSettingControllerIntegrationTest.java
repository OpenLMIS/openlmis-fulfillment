package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

public class FacilityFtpSettingControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/facilityFtpSettings";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

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
    assertEquals(response.getRemoveDirectory(), newSetting.getRemoveDirectory());
    assertEquals(response.getLocalDirectory(), newSetting.getLocalDirectory());
    assertEquals(response.isPassiveMode(), newSetting.isPassiveMode());
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
    setting.setProtocol("ftp");
    setting.setServerHost("host");
    setting.setServerPort(21);
    setting.setRemoveDirectory("remote/dir");
    setting.setLocalDirectory("local/dir");
    setting.setUsername("username");
    setting.setPassword("password");
    setting.setPassiveMode(true);

    return setting;
  }
}
