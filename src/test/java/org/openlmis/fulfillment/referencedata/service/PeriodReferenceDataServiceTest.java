package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.ProcessingPeriodDto;

public class PeriodReferenceDataServiceTest
    extends BaseReferenceDataServiceTest<ProcessingPeriodDto> {

  @Override
  BaseReferenceDataService<ProcessingPeriodDto> getService() {
    return new PeriodReferenceDataService();
  }

  @Override
  ProcessingPeriodDto generateInstance() {
    return new ProcessingPeriodDto();
  }
}
