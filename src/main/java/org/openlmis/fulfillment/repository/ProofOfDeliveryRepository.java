package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProofOfDeliveryRepository extends
    PagingAndSortingRepository<ProofOfDelivery, UUID> {

  ProofOfDelivery findByOrderId(@Param("orderId") UUID orderId);
}

