package org.openlmis.fulfillment.web.validator;

import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

import org.openlmis.fulfillment.web.util.ConfigurationSettingDto;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class ConfigurationSettingValidator implements Validator {

  @Override
  public boolean supports(Class<?> clazz) {
    return ConfigurationSettingDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    rejectIfEmptyOrWhitespace(
        errors, "key", "validation.empty.property.configurationSetting.key",
        "Key property cannot be empty"
    );
    rejectIfEmptyOrWhitespace(
        errors, "value", "validation.empty.property.configurationSetting.value",
        "Value property cannot be empty");
  }

}
