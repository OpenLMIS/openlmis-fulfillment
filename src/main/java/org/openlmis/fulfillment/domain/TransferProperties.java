package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name = "transfer_properties")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("abstract")
@NoArgsConstructor
public abstract class TransferProperties extends BaseEntity implements Storable {

  @Column(nullable = false, unique = true)
  @Getter
  @Setter
  protected UUID facilityId;

  public interface BaseExporter {

    void setId(UUID id);

    void setFacilityId(UUID facilityId);

  }

  public interface BaseImporter {

    UUID getId();

    UUID getFacilityId();

  }

}
