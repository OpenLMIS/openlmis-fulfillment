package org.openlmis.fulfillment.web;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
public abstract class BaseController {

  protected Map<String, String> getErrors(BindingResult bindingResult) {
    return bindingResult
        .getFieldErrors()
        .stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getCode));
  }

}
