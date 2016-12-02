package org.openlmis.fulfillment.service;

import java.io.IOException;

public class JasperReportViewException extends IOException {

  JasperReportViewException(String message, Throwable cause) {
    super(message, cause);
  }
}
