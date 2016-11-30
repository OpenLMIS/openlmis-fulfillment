package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

public interface OrderSender<T> {

  void send(Order order, T arg) throws OrderSenderException;

}
