package org.openlmis.fulfillment.web.validator;

import com.google.common.collect.Lists;

import org.openlmis.fulfillment.util.Message;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
final class ValidationErrors implements Serializable {
  private static final long serialVersionUID = -6919318793720541814L;
  
  private ArrayList<FieldError> errors = Lists.newArrayList();

  void rejectValue(String field, Message.LocalizedMessage message) {
    errors.add(new FieldError(field, message));
  }

  boolean hasErrors() {
    return !errors.isEmpty();
  }
}
