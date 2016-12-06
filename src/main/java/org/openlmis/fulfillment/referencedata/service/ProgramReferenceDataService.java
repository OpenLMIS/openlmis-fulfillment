package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.ProgramDto;
import org.springframework.stereotype.Service;

@Service
public class ProgramReferenceDataService extends BaseReferenceDataService<ProgramDto> {

  @Override
  protected String getUrl() {
    return "/api/programs/";
  }

  @Override
  protected Class<ProgramDto> getResultClass() {
    return ProgramDto.class;
  }

  @Override
  protected Class<ProgramDto[]> getArrayResultClass() {
    return ProgramDto[].class;
  }

}
