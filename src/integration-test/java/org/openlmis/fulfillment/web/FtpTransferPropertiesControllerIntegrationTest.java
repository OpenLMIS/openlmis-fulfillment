package org.openlmis.fulfillment.web;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.fulfillment.domain.FtpProtocol.FTP;
import static org.openlmis.fulfillment.web.util.TransferPropertiesFactory.newInstance;

import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.web.util.TransferPropertiesDto;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

public class FtpTransferPropertiesControllerIntegrationTest
    extends BaseTransferPropertiesControllerIntegrationTest<FtpTransferProperties> {

  @Test
  public void shouldUpdateWithDifferentType() {
    // given
    FtpTransferProperties oldProperties = generateProperties();
    LocalTransferProperties newProperties = new LocalTransferProperties();
    newProperties.setId(oldProperties.getId());
    newProperties.setFacilityId(oldProperties.getFacilityId());
    newProperties.setPath("local/paty");

    given(transferPropertiesRepository.findOne(oldProperties.getId())).willReturn(oldProperties);
    given(transferPropertiesRepository.save(any(TransferProperties.class)))
        .willAnswer(new SaveAnswer<TransferProperties>());

    // when
    TransferPropertiesDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", oldProperties.getId())
        .body(newInstance(newProperties))
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TransferPropertiesDto.class);

    // then
    assertThat(newInstance(response), instanceOf(LocalTransferProperties.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Override
  FtpTransferProperties generateProperties() {
    FtpTransferProperties ftp = new FtpTransferProperties();
    ftp.setId(UUID.randomUUID());
    ftp.setFacilityId(UUID.randomUUID());
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

}
