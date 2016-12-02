package org.openlmis.fulfillment.repository;

import org.apache.commons.lang.RandomStringUtils;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.Random;
import java.util.UUID;

public class FacilityFtpSettingRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<FacilityFtpSetting> {

  @Autowired
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Override
  protected CrudRepository<FacilityFtpSetting, UUID> getRepository() {
    return facilityFtpSettingRepository;
  }

  @Override
  protected FacilityFtpSetting generateInstance() {
    FacilityFtpSetting setting = new FacilityFtpSetting();
    setting.setFacilityId(UUID.randomUUID());
    setting.setServerHost(RandomStringUtils.random(10));
    setting.setServerPort(String.valueOf(new Random().nextInt(9000) + 1000));
    setting.setPath(RandomStringUtils.random(10));
    setting.setUsername(RandomStringUtils.random(10));
    setting.setPassword(RandomStringUtils.random(10));
    return setting;
  }
}
