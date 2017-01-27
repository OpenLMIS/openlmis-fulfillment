package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.util.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ValidationMessage {
  private Message.LocalizedMessage message;
  private List<FieldError> fieldErrors;
}
