package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

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

    Optional.ofNullable(importer.getFacility())
        .ifPresent(facility -> local.setFacilityId(facility.getId()));

    local.path = importer.getPath();

    return local;
  }

  public interface Exporter extends BaseExporter {

    void setPath(String path);

  }

  public interface Importer extends BaseImporter {

    String getPath();

  }

}
