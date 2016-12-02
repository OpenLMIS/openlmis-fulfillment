package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FacilityFtpSettingRepository
    extends PagingAndSortingRepository<FacilityFtpSetting, UUID> {

  FacilityFtpSetting findFirstByFacilityId(@Param("facilityId") UUID facilityId);

}
