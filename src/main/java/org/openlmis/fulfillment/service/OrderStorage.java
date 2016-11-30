package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

public interface OrderStorage {

  void store(Order order) throws OrderStorageException;
}
