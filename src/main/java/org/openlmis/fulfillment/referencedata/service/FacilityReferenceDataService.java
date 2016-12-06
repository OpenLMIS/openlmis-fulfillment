package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.FacilityDto;
import org.springframework.stereotype.Service;

@Service
public class FacilityReferenceDataService extends BaseReferenceDataService<FacilityDto> {

  @Override
  protected String getUrl() {
    return "/api/facilities/";
  }

  @Override
  protected Class<FacilityDto> getResultClass() {
    return FacilityDto.class;
  }

  @Override
  protected Class<FacilityDto[]> getArrayResultClass() {
    return FacilityDto[].class;
  }

}
