package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.ConfigurationSetting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationSettingDto
    implements ConfigurationSetting.Importer, ConfigurationSetting.Exporter {

  private String key;
  private String value;

  /**
   * Creates new instance of {@link ConfigurationSettingDto} based on data from domain class.
   *
   * @param domain instance of {@link ConfigurationSetting}
   * @return new instance of {@link ConfigurationSettingDto}
   */
  public static ConfigurationSettingDto newInstance(ConfigurationSetting domain) {
    ConfigurationSettingDto dto = new ConfigurationSettingDto();
    domain.export(dto);

    return dto;
  }

}
