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

import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.openlmis.fulfillment.repository.ConfigurationSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.NoArgsConstructor;

@Service
@NoArgsConstructor
public class ConfigurationSettingService {

  @Autowired
  private ConfigurationSettingRepository configurationSettingRepository;

  /**
   * Return value for given key if possible.
   *
   * @param key String value indicates key.
   * @return String value of given key.
   */
  public String getStringValue(String key) {
    return getByKey(key).getValue();
  }

  /**
   * Update configuration setting.
   *
   * @param setting contains new value for the given key
   */
  public ConfigurationSetting update(ConfigurationSetting setting) {
    ConfigurationSetting found = getByKey(setting.getKey());
    found.setValue(setting.getValue());

    return configurationSettingRepository.save(found);
  }

  private ConfigurationSetting getByKey(String key) {
    ConfigurationSetting setting = configurationSettingRepository.findOne(key);

    if (null == setting) {
      throw new ConfigurationSettingNotFoundException(key);
    }

    return setting;
  }
}
