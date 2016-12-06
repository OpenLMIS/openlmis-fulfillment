package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.FacilityDto;

public class FacilityReferenceDataServiceTest extends BaseReferenceDataServiceTest<FacilityDto> {

  @Override
  BaseReferenceDataService<FacilityDto> getService() {
    return new FacilityReferenceDataService();
  }

  @Override
  FacilityDto generateInstance() {
    return new FacilityDto();
  }

}
