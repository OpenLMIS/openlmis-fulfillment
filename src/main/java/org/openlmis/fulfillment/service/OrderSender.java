package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

public interface OrderSender {

  boolean send(Order order);

}
