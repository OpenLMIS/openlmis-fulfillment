package org.openlmis.order.repository;

import org.openlmis.order.domain.ProofOfDelivery;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProofOfDeliveryRepository extends
    PagingAndSortingRepository<ProofOfDelivery, UUID> {
}

