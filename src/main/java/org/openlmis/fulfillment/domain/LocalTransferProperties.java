package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("local")
@NoArgsConstructor
public class LocalTransferProperties extends TransferProperties {

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String path;

  /**
   * Creates a new instance of {@link LocalTransferProperties} based on data from {@link Importer}.
   *
   * @param importer instance that implement {@link Importer}
   * @return an instance of {@link LocalTransferProperties}
   */
  public static LocalTransferProperties newInstance(Importer importer) {
    LocalTransferProperties local = new LocalTransferProperties();
    local.id = importer.getId();
    local.facilityId = importer.getFacilityId();
    local.path = importer.getPath();

    return local;
  }

  /**
   * Exports current data from this Local Transfer Properties.
   *
   * @param exporter instance that implement {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setFacilityId(facilityId);
    exporter.setPath(path);
  }

  public interface Exporter extends TransferProperties.Exporter {

    void setPath(String path);

  }

  public interface Importer extends TransferProperties.Importer {

    String getPath();

  }

}
