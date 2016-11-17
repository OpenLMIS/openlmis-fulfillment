package org.openlmis.order.repository;

import org.openlmis.order.domain.OrderNumberConfiguration;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrderNumberConfigurationRepository
    extends PagingAndSortingRepository<OrderNumberConfiguration, UUID> {
}
