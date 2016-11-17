package org.openlmis.order.repository.custom;

import org.openlmis.order.domain.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepositoryCustom {

  List<Order> searchOrders(UUID supplyingFacility, UUID requestingFacility, UUID program);

}
