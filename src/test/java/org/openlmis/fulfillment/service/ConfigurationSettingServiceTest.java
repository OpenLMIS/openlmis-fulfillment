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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationSettingServiceTest {

  @InjectMocks
  private ConfigurationSettingService configurationSettingService;

  @Mock
  private Environment env;

  private static final String SHIPMENT_REASON_KEY = "reasons.shipment";
  private static final String POD_REASON_KEY = "reasons.pod";

  private static final UUID SHIPMENT_REASON_ID = UUID.fromString(
      "c1fc3cf3-da18-44b0-a220-77c985202e06");
  private static final UUID POD_REASON_ID = UUID.fromString(
      "e3fc3cf3-da18-44b0-a220-77c985202e06");

  @Test
  public void shouldReturnReasonIdForShipment_WhenPropertyExists() {
    when(env.getProperty(SHIPMENT_REASON_KEY)).thenReturn(SHIPMENT_REASON_ID.toString());

    UUID result = configurationSettingService.getReasonIdForShipment();

    assertThat(result, is(SHIPMENT_REASON_ID));
  }

  @Test
  public void shouldReturnNullIfReasonIdForShipmentNotProvided() {
    when(env.getProperty(SHIPMENT_REASON_KEY)).thenReturn(null);

    UUID result = configurationSettingService.getReasonIdForShipment();

    assertThat(result, is(nullValue()));
  }

  @Test
  public void shouldReturnReasonIdForProofOfDelivery_WhenPropertyExists() {
    when(env.getProperty(POD_REASON_KEY)).thenReturn(POD_REASON_ID.toString());

    UUID result = configurationSettingService.getReasonIdForProofOfDelivery();

    assertThat(result, is(POD_REASON_ID));
  }
}
