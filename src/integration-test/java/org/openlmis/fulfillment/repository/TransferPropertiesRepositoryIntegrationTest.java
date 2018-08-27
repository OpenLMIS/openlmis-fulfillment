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

package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openlmis.fulfillment.domain.FtpProtocol;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

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
