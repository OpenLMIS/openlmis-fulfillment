package org.openlmis.fulfillment.service;

import org.apache.commons.lang.NullArgumentException;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.referencedata.service.FacilityReferenceDataService;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FacilityFtpSettingService {
  @Autowired
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  /**
   * Retrieves FacilityFtpSetting for given facility.
   * @param facilityId id of facility.
   * @return FacilityFtpSetting entity with matching facility.
   */
  public FacilityFtpSetting getByFacility(UUID facilityId) {
    if (facilityId == null) {
      throw new NullArgumentException("facilityId");
    }

    return facilityFtpSettingRepository.findFirstByFacilityId(facilityId);
  }

  /**
   * Saves an entity of FacilityFtpSetting, first checking if it's facility exists.
   * @param setting an instance of FacilityFtpSetting to be saved.
   * @return saved instance of FacilityFtpSetting.
   */
  public FacilityFtpSetting save(FacilityFtpSetting setting) {
    if (facilityReferenceDataService.findOne(setting.getFacilityId()) == null) {
      throw new IllegalArgumentException("Facility with given ID does not exist.");
    }

    FacilityFtpSetting existent = getByFacility(setting.getFacilityId());
    if (existent != null && existent.getId() != setting.getId()) {
      throw new IllegalArgumentException("A setting for this facility already exists.");
    }

    return facilityFtpSettingRepository.save(setting);
  }
}
