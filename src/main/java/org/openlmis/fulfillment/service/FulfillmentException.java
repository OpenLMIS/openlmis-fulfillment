package org.openlmis.fulfillment.service;

import lombok.Getter;

@Getter
public abstract class FulfillmentException extends Exception {
  private final String messageKey;
  private final String[] params;

  /**
   * Creates new Fulfillment exception with message key and params.
   *
   * @param messageKey key that is related with exception message.
   * @param params     params that will be used in the exception message.
   */
  public FulfillmentException(String messageKey, String... params) {
    super(messageKey);
    this.messageKey = messageKey;
    this.params = params;
  }

  /**
   * Creates new Fulfillment exception with message key and params.
   *
   * @param cause      the cause.
   * @param messageKey key that is related with exception message.
   * @param params     params that will be used in the exception message.
   */
  public FulfillmentException(Throwable cause, String messageKey, String... params) {
    super(messageKey, cause);
    this.messageKey = messageKey;
    this.params = params;
  }

}
