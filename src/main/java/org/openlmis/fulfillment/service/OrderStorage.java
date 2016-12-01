package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

public interface OrderStorage {

  void store(Order order) throws OrderStorageException;

  <T> T get(Order order);

  void delete(Order order) throws OrderStorageException;

}
