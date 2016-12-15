package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.web.util.OrderFileTemplateDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderFileTemplateValidator implements Validator {

  private static final String INVALID_FORMAT_DATE = "Invalid date format";

  private static final String[] ACCEPTED_VALUES = {"MM/yy", "MM/yyyy", "yy/MM", "yyyy/MM",
      "dd/MM/yy", "dd/MM/yyyy", "MM/dd/yy", "MM/dd/yyyy", "yy/MM/dd", "yyyy/MM/dd", "MM-yy",
      "MM-yyyy", "yy-MM", "yyyy-MM", "dd-MM-yy", "dd-MM-yyyy", "MM-dd-yy", "MM-dd-yyyy", "yy-MM-dd",
      "yyyy-MM-dd", "MMyy", "MMyyyy", "yyMM", "yyyyMM", "ddMMyy", "ddMMyyyy", "MMddyy", "MMddyyyy",
      "yyMMdd", "yyyyMMdd"};

  @Override
  public boolean supports(Class<?> clazz) {
    return OrderFileTemplateDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    OrderFileTemplateDto orderFileTemplate = (OrderFileTemplateDto) target;
    List<OrderFileColumn.Importer> columns = orderFileTemplate.getOrderFileColumns();
    List<String> acceptedValues = Arrays.asList(ACCEPTED_VALUES);

    for (int i = 0; i < columns.size(); i++) {
      OrderFileColumn.Importer orderFileColumn = columns.get(i);
      if ((orderFileColumn.getFormat() != null)
          && (!acceptedValues.contains(orderFileColumn.getFormat()))) {
        errors.rejectValue("orderFileColumns[" + i + "].format",
            INVALID_FORMAT_DATE, INVALID_FORMAT_DATE);
      }
    }
  }
}



