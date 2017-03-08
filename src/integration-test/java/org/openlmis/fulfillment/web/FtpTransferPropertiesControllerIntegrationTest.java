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
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.web.util.FtpTransferPropertiesDto;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.springframework.http.MediaType;
import java.util.UUID;

public class FtpTransferPropertiesControllerIntegrationTest
    extends BaseTransferPropertiesControllerIntegrationTest<FtpTransferProperties> {

  private FtpTransferProperties ftpTransferProperties;

  @Before
  public void setUp() {
    ftpTransferProperties = generateProperties();
    given(transferPropertiesRepository.findOne(ftpTransferProperties.getId()))
        .willReturn(ftpTransferProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());
  }


  @Test
  public void shouldUpdateWithDifferentType() {
    // given
    LocalTransferProperties newProperties = new LocalTransferProperties();
    newProperties.setId(ftpTransferProperties.getId());
    newProperties.setFacilityId(ftpTransferProperties.getFacilityId());
    newProperties.setPath("local/path");

    // when
    TransferPropertiesDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ftpTransferProperties.getId())
        .body(newInstance(newProperties, exporter))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    // then
    assertThat(newInstance(response), instanceOf(LocalTransferProperties.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenUpdateWithNegativePort() {
    // given
    ftpTransferProperties.setServerPort(-1);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ftpTransferProperties.getId())
        .body(toDto(ftpTransferProperties))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenCreateWithNegativePort() {
    // given
    ftpTransferProperties.setServerPort(-1);

    // when
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(toDto(ftpTransferProperties))
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Override
  FtpTransferProperties generateProperties() {
    FtpTransferProperties ftp = new FtpTransferProperties();
    ftp.setId(UUID.randomUUID());
    ftp.setFacilityId(UUID.fromString(FACILITY_ID));
    ftp.setProtocol(FTP);
    ftp.setServerHost("host");
    ftp.setServerPort(21);
    ftp.setRemoteDirectory("remote/dir");
    ftp.setLocalDirectory("local/dir");
    ftp.setUsername("username");
    ftp.setPassword("password");
    ftp.setPassiveMode(true);

    return ftp;
  }

  @Override
  void assertTransferProperties(FtpTransferProperties actual) {
    assertThat(actual.getId(), is(notNullValue()));
    assertThat(actual.getFacilityId(), is(notNullValue()));
    assertThat(actual.getProtocol(), is(FTP));
    assertThat(actual.getServerHost(), is("host"));
    assertThat(actual.getServerPort(), is(21));
    assertThat(actual.getRemoteDirectory(), is("remote/dir"));
    assertThat(actual.getLocalDirectory(), is("local/dir"));
    assertThat(actual.getUsername(), is("username"));
    assertThat(actual.getPassiveMode(), is(true));
  }

  @Override
  TransferPropertiesDto toDto(FtpTransferProperties properties) {
    FtpTransferPropertiesDto propertiesDto =
        (FtpTransferPropertiesDto) newInstance(properties, exporter);
    propertiesDto.setPassword(properties.getPassword());
    return propertiesDto;
  }

}
