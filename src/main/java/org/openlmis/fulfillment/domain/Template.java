package org.openlmis.fulfillment.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "templates")
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Template extends BaseEntity {

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION, unique = true, nullable = false)
  @Getter
  @Setter
  private String name;

  @Column
  @Getter
  @Setter
  private byte[] data;

  @OneToMany(
      mappedBy = "template",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Fetch(FetchMode.SELECT)
  @Getter
  @Setter
  private List<TemplateParameter> templateParameters;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String type;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String description;

  @PrePersist
  private void prePersist() {
    forEachParameter(line -> line.setTemplate(this));
  }

  @PreUpdate
  private void preUpdate() {
    forEachParameter(line -> line.setTemplate(this));
  }

  /**
   * Copy values of attributes into new or updated Template.
   *
   * @param template Template with new values.
   */
  public void updateFrom(Template template) {
    this.name = template.getName();
    this.data = template.getData();
    this.templateParameters = template.getTemplateParameters();
    this.type = template.getType();
    this.description = template.getDescription();
  }

  public void forEachParameter(Consumer<TemplateParameter> consumer) {
    Optional.ofNullable(templateParameters)
        .ifPresent(list -> list.forEach(consumer));
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */

  public void export(Template.Exporter exporter) {
    exporter.setData(data);
    exporter.setDescription(description);
    exporter.setId(id);
    exporter.setName(name);
    exporter.setType(type);
  }

  public interface Exporter {
    void setId(UUID id);

    void setName(String name);

    void setData(byte[] data);

    void setType(String type);

    void setDescription(String description);

  }

  public interface Importer {
    UUID getId();

    String getName();

    byte[] getData();

    String getType();

    String getDescription();

    List<TemplateParameter.Importer> getTemplateParameters();

  }
}
