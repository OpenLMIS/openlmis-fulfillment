package org.openlmis.order.repository;

import org.openlmis.order.domain.Order;
import org.openlmis.order.repository.custom.OrderRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderRepository extends PagingAndSortingRepository<Order, UUID>,
    OrderRepositoryCustom {
}
