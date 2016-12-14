package org.openlmis.fulfillment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "configuration_settings")
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationSetting {

  @Id
  @Getter
  @Setter
  private String key;

  @Column(nullable = false)
  @Getter
  @Setter
  private String value;

  /**
   * Creates a new instance of {@link ConfigurationSetting} based on data from dto.
   *
   * @param importer configuration settings.
   * @return new instance of {@link ConfigurationSetting}.
   */
  public static ConfigurationSetting newInstance(Importer importer) {
    return new ConfigurationSetting(importer.getKey(), importer.getValue());
  }

  /**
   * Exports current state of this configuration setting.
   *
   * @param exporter an instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setKey(key);
    exporter.setValue(value);
  }

  public interface Importer {

    String getKey();

    String getValue();

  }

  public interface Exporter {

    void setKey(String key);

    void setValue(String value);

  }

}
