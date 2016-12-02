package org.openlmis.fulfillment.repository.custom;

import java.util.List;
import java.util.UUID;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;

public interface FacilityFtpSettingRepositoryCustom {
  List<FacilityFtpSetting> searchFacilityFtpSettings(UUID facility);
}
