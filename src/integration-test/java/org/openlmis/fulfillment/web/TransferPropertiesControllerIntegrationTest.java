package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpProtocol;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
public class TransferPropertiesControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String ACCESS_TOKEN = "access_token";
  private static final String RESOURCE_URL = "/api/transferProperties";
  private static final String ID_URL = RESOURCE_URL + "/{id}";

  @MockBean
  private TransferPropertiesRepository transferPropertiesRepository;

  @Test
  public void shouldCreateFtpProperties() {
    // given
    FtpTransferProperties properties = generateFtpProperties();
    given(transferPropertiesRepository.save(any(FtpTransferProperties.class)))
        .willAnswer(new SaveAnswer<FtpTransferProperties>());

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
  public void shouldCreateLocalProperties() {
    // given
    LocalTransferProperties properties = generateLocalProperties();
    given(transferPropertiesRepository.save(any(LocalTransferProperties.class)))
        .willAnswer(new SaveAnswer<LocalTransferProperties>());

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
  public void shouldUpdateFtpProperties() {
    // given
    FtpTransferProperties oldProperties = generateFtpProperties();
    FtpTransferProperties newProperties = generateFtpProperties();
    newProperties.setId(oldProperties.getId());

    when(transferPropertiesRepository.findOne(oldProperties.getId())).thenReturn(oldProperties);
    given(transferPropertiesRepository.save(any(FtpTransferProperties.class)))
        .willAnswer(new SaveAnswer<FtpTransferProperties>());

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
    assertEquals(response.getId(), oldProperties.getId());
    assertEquals(response.getFacilityId(), newProperties.getFacilityId());
    assertEquals(response.getServerHost(), newProperties.getServerHost());
    assertEquals(response.getServerPort(), newProperties.getServerPort());
    assertEquals(response.getRemoteDirectory(), newProperties.getRemoteDirectory());
    assertEquals(response.getLocalDirectory(), newProperties.getLocalDirectory());
    assertEquals(response.getPassiveMode(), newProperties.getPassiveMode());
    assertNull(response.getPath());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateLocalProperties() {
    // given
    LocalTransferProperties oldProperties = generateLocalProperties();
    LocalTransferProperties newProperties = generateLocalProperties();
    newProperties.setId(oldProperties.getId());

    when(transferPropertiesRepository.findOne(oldProperties.getId())).thenReturn(oldProperties);
    given(transferPropertiesRepository.save(any(LocalTransferProperties.class)))
        .willAnswer(new SaveAnswer<LocalTransferProperties>());

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
    assertEquals(response.getId(), oldProperties.getId());
    assertEquals(response.getFacilityId(), newProperties.getFacilityId());
    assertNull(response.getServerHost());
    assertNull(response.getServerPort());
    assertNull(response.getRemoteDirectory());
    assertNull(response.getLocalDirectory());
    assertNull(response.getPassiveMode());
    assertEquals(response.getPath(), newProperties.getPath());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteFtpProperties() {
    // given
    FtpTransferProperties properties = generateFtpProperties();
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
  public void shouldDeleteLocalProperties() {
    // given
    LocalTransferProperties properties = generateLocalProperties();
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
  public void shouldNotDeleteNonexistentFtpProperties() {
    // given
    FtpTransferProperties properties = generateFtpProperties();
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
  public void shouldNotDeleteNonexistentLocalProperties() {
    // given
    LocalTransferProperties properties = generateLocalProperties();
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
  public void shouldGetChosenFtpProperties() {
    // given
    FtpTransferProperties properties = generateFtpProperties();
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
    assertEquals(response.getId(), properties.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenLocalProperties() {
    // given
    LocalTransferProperties properties = generateLocalProperties();
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
    assertEquals(response.getId(), properties.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentFtpProperties() {
    // given
    FtpTransferProperties properties = generateFtpProperties();
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
  public void shouldNotGetNonexistentLocalProperties() {
    // given
    LocalTransferProperties properties = generateLocalProperties();
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

  private FtpTransferProperties generateFtpProperties() {
    FtpTransferProperties ftp = new FtpTransferProperties();
    ftp.setId(UUID.randomUUID());
    ftp.setFacilityId(UUID.randomUUID());
    ftp.setProtocol(FtpProtocol.FTP);
    ftp.setServerHost("host");
    ftp.setServerPort(21);
    ftp.setRemoteDirectory("remote/dir");
    ftp.setLocalDirectory("local/dir");
    ftp.setUsername("username");
    ftp.setPassword("password");
    ftp.setPassiveMode(true);

    return ftp;
  }

  private LocalTransferProperties generateLocalProperties() {
    LocalTransferProperties local = new LocalTransferProperties();
    local.setId(UUID.randomUUID());
    local.setFacilityId(UUID.randomUUID());
    local.setPath("local/dir");

    return local;
  }
}
