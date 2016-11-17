package org.openlmis.order.repository;

import org.openlmis.order.domain.Requisition;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface RequisitionRepository extends
    PagingAndSortingRepository<Requisition, UUID> {
}
