package org.openlmis.order.repository;

import org.openlmis.order.domain.OrderFileColumn;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderFileColumnRepository extends
    PagingAndSortingRepository<OrderFileColumn, UUID> {
}

