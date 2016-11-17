package org.openlmis.order.repository;

import org.openlmis.order.domain.OrderFileTemplate;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderFileTemplateRepository extends
    PagingAndSortingRepository<OrderFileTemplate, UUID> {
}
