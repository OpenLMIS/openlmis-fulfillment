package org.openlmis.fulfillment.web.errorhandler;

import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.util.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base classes for controller advices dealing with error handling.
 */
abstract class AbstractErrorHandling {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private MessageService messageService;

  Message.LocalizedMessage logErrorAndRespond(String message, String messageKey, String... params) {
    return logErrorAndRespond(message, new FulfillmentException(messageKey, params));
  }

  /**
   * Logs an error message and returns an error response.
   *
   * @param message the error message
   * @param ex      the exception to log. Message from the exception is used as the error
   *                description.
   * @return a LocalizedMessage that should be sent to the client
   */
  Message.LocalizedMessage logErrorAndRespond(String message, FulfillmentException ex) {
    logger.info(message, ex);
    return getLocalizedMessage(ex);
  }

  /**
   * Translate the Message in a FulfillmentException into a LocalizedMessage.
   *
   * @param exception is any FulfillmentException containing a Message
   * @return a LocalizedMessage translated by the MessageService bean
   */
  protected final Message.LocalizedMessage getLocalizedMessage(FulfillmentException exception) {
    return messageService.localize(exception.asMessage());
  }

}
