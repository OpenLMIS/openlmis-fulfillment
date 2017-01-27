package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.util.Message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class FieldError {
  private final String field;
  private final Message.LocalizedMessage error;
}
