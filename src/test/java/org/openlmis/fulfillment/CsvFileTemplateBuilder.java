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

package org.openlmis.fulfillment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.openlmis.fulfillment.domain.CsvFileColumn;
import org.openlmis.fulfillment.domain.CsvFileTemplate;
import org.openlmis.fulfillment.domain.CsvTemplateType;

@NoArgsConstructor
public class CsvFileTemplateBuilder {

  private UUID id = UUID.randomUUID();
  private String filePrefix = "O";
  private Boolean headerInFile = true;
  private CsvTemplateType templateType = CsvTemplateType.ORDER;
  private List<CsvFileColumn> csvFileColumns = new ArrayList<>();

  /**
   * Creates a new CsvFileTemplate object.
   * @return @CsvFileTemplate
   */
  public CsvFileTemplate build() {
    CsvFileTemplate template = new CsvFileTemplate(filePrefix, headerInFile, templateType,
        csvFileColumns);
    template.setId(id);
    return template;
  }

}
