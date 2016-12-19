package org.openlmis.fulfillment.web.errorhandler;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
  private String message;
  private String messageKey;
  private String[] params;
}
