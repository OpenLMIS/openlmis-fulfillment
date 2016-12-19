package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

@FunctionalInterface
public interface OrderSender {

  boolean send(Order order);

}
