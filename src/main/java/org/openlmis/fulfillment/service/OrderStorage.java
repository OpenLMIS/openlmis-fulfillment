package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;

import java.nio.file.Path;

public interface OrderStorage {

  void store(Order order) throws OrderStorageException;

  void delete(Order order) throws OrderStorageException;

  default Path getOrderAsPath(Order order) {
    throw new UnsupportedOperationException();
  }

}
