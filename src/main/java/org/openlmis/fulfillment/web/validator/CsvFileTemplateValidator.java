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

package org.openlmis.fulfillment.web.validator;

import java.util.Arrays;
import java.util.List;

import org.openlmis.fulfillment.domain.CsvFileColumn;
import org.openlmis.fulfillment.web.util.CsvFileTemplateDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CsvFileTemplateValidator implements Validator {

  private static final String INVALID_FORMAT_DATE = "Invalid date format";

  private static final String[] ACCEPTED_VALUES = {"MM/yy", "MM/yyyy", "yy/MM", "yyyy/MM",
      "dd/MM/yy", "dd/MM/yyyy", "MM/dd/yy", "MM/dd/yyyy", "yy/MM/dd", "yyyy/MM/dd", "MM-yy",
      "MM-yyyy", "yy-MM", "yyyy-MM", "dd-MM-yy", "dd-MM-yyyy", "MM-dd-yy", "MM-dd-yyyy", "yy-MM-dd",
      "yyyy-MM-dd", "MMyy", "MMyyyy", "yyMM", "yyyyMM", "ddMMyy", "ddMMyyyy", "MMddyy", "MMddyyyy",
      "yyMMdd", "yyyyMMdd"};

  @Override
  public boolean supports(Class<?> clazz) {
    return CsvFileTemplateDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    CsvFileTemplateDto orderFileTemplate = (CsvFileTemplateDto) target;
    List<CsvFileColumn.Importer> columns = orderFileTemplate.getCsvFileColumns();
    List<String> acceptedValues = Arrays.asList(ACCEPTED_VALUES);

    for (int i = 0; i < columns.size(); i++) {
      CsvFileColumn.Importer orderFileColumn = columns.get(i);
      if ((orderFileColumn.getFormat() != null)
          && (!acceptedValues.contains(orderFileColumn.getFormat()))) {
        errors.rejectValue("csvFileColumns[" + i + "].format",
            INVALID_FORMAT_DATE, INVALID_FORMAT_DATE);
      }
    }
  }
}



