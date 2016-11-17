package org.openlmis.order.repository;

import org.openlmis.order.domain.ProofOfDeliveryLineItem;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface ProofOfDeliveryLineItemRepository extends
    PagingAndSortingRepository<ProofOfDeliveryLineItem, UUID> {
}
