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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.openlmis.fulfillment.repository.ConfigurationSettingRepository;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationSettingServiceTest {
  private static final String KEY = "key";
  private static final String VALUE = "value";

  @Mock
  private ConfigurationSettingRepository repository;

  @InjectMocks
  private ConfigurationSettingService service;

  @Mock
  private ConfigurationSetting setting;

  @Test(expected = ConfigurationSettingNotFoundException.class)
  public void shouldThrowExceptionIfSettingCannotBeFound() {
    service.getStringValue(KEY);
  }

  @Test
  public void shouldReturnStringValue() {
    when(repository.findOne(KEY)).thenReturn(setting);
    when(setting.getValue()).thenReturn(VALUE);

    assertThat(service.getStringValue(KEY), is(VALUE));
  }

  @Test
  public void shouldUpdateSetting() throws Exception {
    when(repository.findOne(KEY)).thenReturn(setting);
    when(repository.save(any(ConfigurationSetting.class))).thenReturn(setting);

    ConfigurationSetting setting = new ConfigurationSetting();
    setting.setKey(KEY);
    setting.setValue(VALUE + "2");

    service.update(setting);

    verify(this.setting).setValue(VALUE + "2");
    verify(repository).save(this.setting);

  }

  @Test
  public void shouldCatchExceptionAndReturnFalseIfKeyDoesNotExists()
          throws ConfigurationSettingException {
    assertEquals(service.getBoolValue("testEmpty"), Boolean.FALSE);
  }

  @Test
  public void shouldGetBoolTrueValueIfKeyExists() {
    setting = new ConfigurationSetting();
    setting.setKey("testTrue");
    setting.setValue(Boolean.TRUE.toString());
    when(repository
            .findOne(setting.getKey()))
            .thenReturn(setting);
    assertEquals(service.getBoolValue("testTrue"), Boolean.TRUE);
  }

  @Test
  public void shouldGetBoolFalseValueIfKeyExists() {
    ConfigurationSetting setting = new ConfigurationSetting();
    setting.setKey("testFalse");
    setting.setValue(Boolean.FALSE.toString());
    when(repository
            .findOne(setting.getKey()))
            .thenReturn(setting);
    assertEquals(service.getBoolValue("testFalse"), Boolean.FALSE);
  }
}
