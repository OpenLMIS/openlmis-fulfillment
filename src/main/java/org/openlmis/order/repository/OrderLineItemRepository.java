package org.openlmis.order.repository;

import org.openlmis.order.domain.OrderLineItem;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderLineItemRepository extends PagingAndSortingRepository<OrderLineItem, UUID> {
}
