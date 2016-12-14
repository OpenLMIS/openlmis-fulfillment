package org.openlmis.fulfillment.service.referencedata;

public class FacilityReferenceDataServiceTest extends BaseReferenceDataServiceTest<FacilityDto> {

  @Override
  protected BaseReferenceDataService<FacilityDto> getService() {
    return new FacilityReferenceDataService();
  }

  @Override
  FacilityDto generateInstance() {
    return new FacilityDto();
  }

}
