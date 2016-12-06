package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.ProgramDto;

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
