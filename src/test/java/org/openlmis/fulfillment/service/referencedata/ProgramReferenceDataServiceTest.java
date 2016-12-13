package org.openlmis.fulfillment.service.referencedata;

public class ProgramReferenceDataServiceTest extends BaseReferenceDataServiceTest<ProgramDto> {

  @Override
  BaseReferenceDataService<ProgramDto> getService() {
    return new ProgramReferenceDataService();
  }

  @Override
  ProgramDto generateInstance() {
    return new ProgramDto();
  }
}
