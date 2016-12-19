package org.openlmis.fulfillment.web.errorhandler;

import org.openlmis.fulfillment.i18n.ExposedMessageSource;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Base classes for controller advices dealing with error handling.
 */
abstract class AbstractErrorHandling {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private ExposedMessageSource messageSource;

  /**
   * Logs an error message and returns an error response.
   *
   * @param message the error message
   * @param ex      the exception to log. Message from the exception is used as the error
   *                description.
   * @return the error response that should be sent to the client
   */
  ErrorResponse logErrorAndRespond(String message, FulfillmentException ex) {
    logger.debug(message, ex);
    return new ErrorResponse(getMessage(ex), ex.getMessageKey(), ex.getParams());
  }

  private String getMessage(FulfillmentException ex) {
    return messageSource.getMessage(
        ex.getMessageKey(), ex.getParams(), LocaleContextHolder.getLocale()
    );
  }

}
