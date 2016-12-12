package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpProtocol;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.Random;
import java.util.UUID;

public class TransferPropertiesRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<TransferProperties> {

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Override
  protected CrudRepository<TransferProperties, UUID> getRepository() {
    return transferPropertiesRepository;
  }

  @Override
  protected TransferProperties generateInstance() {
    FtpTransferProperties setting = new FtpTransferProperties();
    setting.setProtocol(FtpProtocol.FTP);
    setting.setFacilityId(UUID.randomUUID());
    setting.setServerHost(RandomStringUtils.random(10));
    setting.setServerPort(new Random().nextInt(9000) + 1000);
    setting.setRemoteDirectory(RandomStringUtils.random(10));
    setting.setLocalDirectory(RandomStringUtils.random(10));
    setting.setUsername(RandomStringUtils.random(10));
    setting.setPassword(RandomStringUtils.random(10));
    setting.setPassiveMode(true);

    return setting;
  }

  @Test
  public void shouldFindSettingByFacilityId() {
    UUID facilityId = UUID.randomUUID();

    TransferProperties setting = generateInstance();
    setting.setFacilityId(facilityId);

    transferPropertiesRepository.save(setting);

    TransferProperties found = transferPropertiesRepository.findFirstByFacilityId(facilityId);

    assertThat(found.getId(), is(setting.getId()));
  }

}
