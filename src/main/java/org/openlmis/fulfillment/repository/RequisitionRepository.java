package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.Requisition;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface RequisitionRepository extends
    PagingAndSortingRepository<Requisition, UUID> {
}
