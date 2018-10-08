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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "csv_file_columns")
@NoArgsConstructor
@AllArgsConstructor
public class CsvFileColumn extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean openLmisField;

  @Getter
  @Setter
  private String dataFieldLabel;

  @Getter
  @Setter
  private String columnLabel;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean include;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer position;

  @Getter
  @Setter
  private String format;

  @Getter
  @Setter
  private String nested;

  @Getter
  @Setter
  private String keyPath;

  @Getter
  @Setter
  private String related;

  @Getter
  @Setter
  private String relatedKeyPath;

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "csvFileTemplateId", nullable = false)
  @Getter
  @Setter
  private CsvFileTemplate csvFileTemplate;

  /**
   * Creates new CsvFileColumn object based on data from {@link CsvFileColumn.Importer}.
   *
   * @param importer instance of {@link CsvFileColumn.Importer}
   * @return new instance of CsvFileColumn.
   */
  public static CsvFileColumn newInstance(Importer importer) {
    CsvFileColumn csvFileColumn = new CsvFileColumn();
    csvFileColumn.setId(importer.getId());
    csvFileColumn.setOpenLmisField(importer.getOpenLmisField());
    csvFileColumn.setDataFieldLabel(importer.getDataFieldLabel());
    csvFileColumn.setColumnLabel(importer.getColumnLabel());
    csvFileColumn.setInclude(importer.getInclude());
    csvFileColumn.setPosition(importer.getPosition());
    csvFileColumn.setNested(importer.getNested());
    csvFileColumn.setKeyPath(importer.getKeyPath());
    csvFileColumn.setRelated(importer.getRelated());
    csvFileColumn.setRelatedKeyPath(importer.getRelatedKeyPath());
    csvFileColumn.setFormat(importer.getFormat());
    return csvFileColumn;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setFormat(format);
    exporter.setColumnLabel(columnLabel);
    exporter.setOpenLmisField(openLmisField);
    exporter.setDataFieldLabel(dataFieldLabel);
    exporter.setInclude(include);
    exporter.setRelated(related);
    exporter.setPosition(position);
    exporter.setNested(nested);
    exporter.setRelatedKeyPath(relatedKeyPath);
    exporter.setKeyPath(keyPath);

  }

  public interface Exporter {
    void setId(UUID id);

    void setOpenLmisField(Boolean openLmisField);

    void setDataFieldLabel(String dataFieldLabel);

    void setColumnLabel(String columnLabel);

    void setInclude(Boolean include);

    void setPosition(Integer position);

    void setFormat(String format);

    void setNested(String nested);

    void setKeyPath(String keyPath);

    void setRelated(String related);

    void setRelatedKeyPath(String relatedKeyPath);

  }

  public interface Importer {
    UUID getId();

    Boolean getOpenLmisField();

    String getDataFieldLabel();

    String getColumnLabel();

    Boolean getInclude();

    Integer getPosition();

    String getFormat();

    String getNested();

    String getKeyPath();

    String getRelated();

    String getRelatedKeyPath();

  }

}
