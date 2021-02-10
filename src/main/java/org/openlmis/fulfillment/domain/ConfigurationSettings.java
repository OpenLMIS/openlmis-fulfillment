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

package org.openlmis.fulfillment.domain;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuration_settings")
public final class ConfigurationSettings {

  @Getter
  @Setter
  @Column(nullable = false)
  @Id
  private String key;

  @Getter
  @Setter
  @Column(nullable = false)
  private String value;

  /**
   * Create a new instance of ConfigurationSettings based on data
   * from {@link ConfigurationSettings.Importer}.
   *
   * @param importer instance of {@link ConfigurationSettings.Importer}.
   * @return new instance of configurationSettings.
   */
  public static ConfigurationSettings newInstance(Importer importer) {
    ConfigurationSettings configurationSettings = new ConfigurationSettings();
    configurationSettings.setKey(importer.getKey());
    configurationSettings.setValue(importer.getKey());
    return configurationSettings;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setKey(key);
    exporter.setValue(value);
  }

  public interface Exporter {
    void setId(UUID id);

    void setKey(String key);

    void setValue(String value);

  }

  public interface Importer {
    UUID getId();

    String getKey();

    String getValue();

  }
}
