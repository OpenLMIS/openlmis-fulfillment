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

package org.openlmis.fulfillment.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootApplication(scanBasePackages = "org.openlmis.fulfillment")
@SpringBootTest
@TestPropertySource(properties = {
    "reasons.pod=e3fc3cf3-da18-44b0-a220-77c985202e06",
    "reasons.shipment=c1fc3cf3-da18-44b0-a220-77c985202e06"})
@ActiveProfiles({"test", "test-run"})
public class ConfigurationSettingServiceIntegrationTest {
  private static final UUID POD_REASON_ID = UUID.fromString(
      "e3fc3cf3-da18-44b0-a220-77c985202e06");
  private static final UUID SHIPMENT_REASON_ID = UUID.fromString(
      "c1fc3cf3-da18-44b0-a220-77c985202e06");

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Test
  public void shouldReturnReasonIdForProofOfDelivery() {
    assertThat(configurationSettingService.getReasonIdForProofOfDelivery(), is(POD_REASON_ID));
  }

  @Test
  public void shouldReturnReasonIdForShipment() {
    assertThat(configurationSettingService.getReasonIdForShipment(), is(SHIPMENT_REASON_ID));
  }
}
