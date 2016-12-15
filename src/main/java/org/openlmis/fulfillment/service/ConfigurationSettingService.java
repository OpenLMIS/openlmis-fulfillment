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
   * @throws ConfigurationSettingException when setting was not found or value is null.
   */
  public String getStringValue(String key) throws ConfigurationSettingException {
    return getByKey(key).getValue();
  }

  /**
   * Update configuration setting.
   *
   * @param setting contains new value for the given key
   * @throws ConfigurationSettingException when setting was not found.
   */
  public ConfigurationSetting update(ConfigurationSetting setting)
      throws ConfigurationSettingException {
    ConfigurationSetting found = getByKey(setting.getKey());
    found.setValue(setting.getValue());

    return configurationSettingRepository.save(found);
  }

  private ConfigurationSetting getByKey(String key) throws ConfigurationSettingException {
    ConfigurationSetting setting = configurationSettingRepository.findOne(key);

    if (null == setting) {
      throw new ConfigurationSettingNotFoundException(key);
    }

    return setting;
  }

}
