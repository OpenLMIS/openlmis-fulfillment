package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.dto.OrderNumberConfigurationDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OrderNumberConfigurationValidator implements Validator {

  private static final String INVALID_PREFIX =
      "Prefix should be alphanumeric and at most 8 characters long";
  private static final String PREFIX_FIELD = "orderNumberPrefix";

  @Override
  public boolean supports(Class<?> clazz) {
    return OrderNumberConfigurationDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    OrderNumberConfigurationDto orderNumberConfiguration = (OrderNumberConfigurationDto) target;
    if (!validateOrderNumberPrefix(orderNumberConfiguration.getOrderNumberPrefix())) {
      errors.rejectValue(PREFIX_FIELD, INVALID_PREFIX);
    }
  }

  private Boolean validateOrderNumberPrefix(String prefix) {
    return prefix.matches("^[a-zA-Z0-9]{1,8}$");
  }

}
