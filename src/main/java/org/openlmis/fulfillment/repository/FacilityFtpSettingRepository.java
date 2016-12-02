package org.openlmis.fulfillment.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.repository.custom.FacilityFtpSettingRepositoryCustom;

public interface FacilityFtpSettingRepository
    extends PagingAndSortingRepository<FacilityFtpSetting, UUID>,
    FacilityFtpSettingRepositoryCustom {
}
