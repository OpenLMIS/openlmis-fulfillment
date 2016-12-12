package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.TransferProperties;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransferPropertiesRepository
    extends PagingAndSortingRepository<TransferProperties, UUID> {

  TransferProperties findFirstByFacilityId(@Param("facilityId") UUID facilityId);

}
