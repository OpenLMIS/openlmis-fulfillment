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

import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "csv_file_templates")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class CsvFileTemplate extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  private String filePrefix;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean headerInFile;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Getter
  @Setter
  private CsvTemplateType templateType;

  @OneToMany(
      mappedBy = "csvFileTemplate",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @OrderBy("position ASC")
  @Getter
  @Setter
  private List<CsvFileColumn> csvFileColumns;

  /**
   * Updates itself using data from {@link CsvFileTemplate.Importer}.
   *
   * @param importer instance of {@link CsvFileTemplate.Importer}
   */
  public void importDto(Importer importer) {
    id = importer.getId();
    filePrefix = importer.getFilePrefix();
    headerInFile = importer.getHeaderInFile();
    templateType = importer.getTemplateType();

    csvFileColumns.clear();
    if (importer.getCsvFileColumns() != null) {
      for (CsvFileColumn.Importer columnImporter : importer.getCsvFileColumns()) {
        CsvFileColumn column = CsvFileColumn.newInstance(columnImporter);
        column.setCsvFileTemplate(this);

        csvFileColumns.add(column);
      }
    }
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setFilePrefix(filePrefix);
    exporter.setHeaderInFile(headerInFile);
    exporter.setTemplateType(templateType);
  }

  public interface Exporter {
    void setId(UUID id);

    void setFilePrefix(String filePrefix);

    void setHeaderInFile(Boolean headerInFile);

    void setTemplateType(CsvTemplateType templateType);

  }

  public interface Importer {
    UUID getId();

    String getFilePrefix();

    Boolean getHeaderInFile();

    List<CsvFileColumn.Importer> getCsvFileColumns();

    CsvTemplateType getTemplateType();

  }
}
