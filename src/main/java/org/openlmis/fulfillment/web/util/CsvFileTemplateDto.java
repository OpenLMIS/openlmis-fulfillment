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

package org.openlmis.fulfillment.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.fulfillment.domain.CsvFileColumn;
import org.openlmis.fulfillment.domain.CsvFileTemplate;
import org.openlmis.fulfillment.domain.CsvTemplateType;

@AllArgsConstructor
@NoArgsConstructor
public class CsvFileTemplateDto implements CsvFileTemplate.Importer,
    CsvFileTemplate.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private String filePrefix;

  @Getter
  @Setter
  private Boolean headerInFile;

  @Setter
  private List<CsvFileColumnDto> csvFileColumns;

  @Getter
  @Setter
  private CsvTemplateType templateType;

  @Override
  public List<CsvFileColumn.Importer> getCsvFileColumns() {
    return new ArrayList<>(
        Optional.ofNullable(csvFileColumns).orElse(Collections.emptyList())
    );
  }

  /**
   * Create new list of CsvFileTemplateDto based on given list of {@link CsvFileTemplate}.
   * @param templates instance of CsvFileTemplate
   * @return new instance of CsvFileTemplateDto.
   */
  public static Iterable<CsvFileTemplateDto> newInstance(Iterable<CsvFileTemplate> templates) {

    List<CsvFileTemplateDto> csvFileTemplateDtos = new ArrayList<>();
    templates.forEach(t -> csvFileTemplateDtos.add(newInstance(t)));
    return csvFileTemplateDtos;
  }

  /**
   * Create new instance of CsvFileTemplateDto based on given {@link CsvFileTemplate}.
   * @param csvFileTemplate instance of CsvFileTemplate
   * @return new instance of CsvFileTemplateDto.
   */
  public static CsvFileTemplateDto newInstance(CsvFileTemplate csvFileTemplate) {
    CsvFileTemplateDto csvFileTemplateDto = new CsvFileTemplateDto();
    csvFileTemplate.export(csvFileTemplateDto);

    if (csvFileTemplate.getCsvFileColumns() != null) {
      csvFileTemplateDto.setCsvFileColumns(csvFileTemplate.getCsvFileColumns()
          .stream().map(CsvFileColumnDto::newInstance).collect(Collectors.toList()));
    }
    return csvFileTemplateDto;
  }
}
