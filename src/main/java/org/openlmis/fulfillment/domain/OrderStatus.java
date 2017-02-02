package org.openlmis.fulfillment.domain;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

public enum OrderStatus {
  ORDERED,
  IN_TRANSIT,
  PICKING,
  PICKED,
  SHIPPED,
  RECEIVED,
  TRANSFER_FAILED,
  IN_ROUTE,
  READY_TO_PACK;

  /**
   * Find a correct {@link OrderStatus} instance based on the passed string. The method ignores
   * the case.
   *
   * @param arg string representation of one of order status.
   * @return instance of {@link OrderStatus} if the given string matches status; otherwise null.
   */
  public static OrderStatus fromString(String arg) {
    for (OrderStatus status : values()) {
      if (equalsIgnoreCase(arg, status.name())) {
        return status;
      }
    }

    return null;
  }
}
