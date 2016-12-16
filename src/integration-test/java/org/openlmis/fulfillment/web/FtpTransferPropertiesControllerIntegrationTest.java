package org.openlmis.fulfillment.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.openlmis.fulfillment.domain.FtpProtocol.FTP;

import org.openlmis.fulfillment.domain.FtpTransferProperties;

import java.util.UUID;

public class FtpTransferPropertiesControllerIntegrationTest
    extends BaseTransferPropertiesControllerIntegrationTest<FtpTransferProperties> {

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
