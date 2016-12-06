package org.openlmis.fulfillment.referencedata.service;

import org.openlmis.fulfillment.referencedata.model.OrderableProductDto;

public class OrderableProductReferenceDataServiceTest
    extends BaseReferenceDataServiceTest<OrderableProductDto> {

  @Override
  BaseReferenceDataService<OrderableProductDto> getService() {
    return new OrderableProductReferenceDataService();
  }

  @Override
  OrderableProductDto generateInstance() {
    return new OrderableProductDto();
  }
}
