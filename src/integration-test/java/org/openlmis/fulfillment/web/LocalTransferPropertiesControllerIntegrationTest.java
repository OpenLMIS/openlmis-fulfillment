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

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.fulfillment.domain.FtpProtocol.FTP;
import static org.openlmis.fulfillment.web.util.TransferPropertiesFactory.newInstance;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.web.util.FtpTransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class LocalTransferPropertiesControllerIntegrationTest
    extends BaseTransferPropertiesControllerIntegrationTest<LocalTransferProperties> {

  private static final String LOCAL_DIR = "local/dir";

  private LocalTransferProperties localTransferProperties;

  @Before
  public void setUp() {
    localTransferProperties = generateProperties();
    given(transferPropertiesRepository.findOne(localTransferProperties.getId()))
        .willReturn(localTransferProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());
  }


  @Test
  public void shouldUpdateWithDifferentType() {
    // given
    FtpTransferProperties newProperties = new FtpTransferProperties();
    newProperties.setId(localTransferProperties.getId());
    newProperties.setFacilityId(localTransferProperties.getFacilityId());
    newProperties.setProtocol(FTP);
    newProperties.setServerHost("host");
    newProperties.setServerPort(21);
    newProperties.setRemoteDirectory("remote/dir");
    newProperties.setLocalDirectory(LOCAL_DIR);
    newProperties.setUsername("username");
    newProperties.setPassword("password");
    newProperties.setPassiveMode(true);

    // when
    FtpTransferPropertiesDto propertiesDto =
        (FtpTransferPropertiesDto) newInstance(newProperties, exporter);
    propertiesDto.setPassword(newProperties.getPassword());
    TransferPropertiesDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", localTransferProperties.getId())
        .body(propertiesDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    // then
    assertThat(newInstance(response), instanceOf(FtpTransferProperties.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdateWithBlankPath() {
    // given
    localTransferProperties.setPath("");

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", localTransferProperties.getId())
        .body(newInstance(localTransferProperties, exporter))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenCreateWithBlankPath() {
    // given
    localTransferProperties.setPath("");

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(newInstance(localTransferProperties, exporter))
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Override
  LocalTransferProperties generateProperties() {
    LocalTransferProperties local = new LocalTransferProperties();
    local.setId(UUID.randomUUID());
    local.setFacilityId(UUID.fromString(FACILITY_ID));
    local.setPath(LOCAL_DIR);

    return local;
  }

  @Override
  void assertTransferProperties(LocalTransferProperties actual) {
    assertThat(actual.getId(), is(notNullValue()));
    assertThat(actual.getFacilityId(), is(notNullValue()));
    assertThat(actual.getPath(), is(LOCAL_DIR));
  }

  @Override
  TransferPropertiesDto toDto(LocalTransferProperties properties) {
    return TransferPropertiesFactory.newInstance(properties, exporter);

  }

}
