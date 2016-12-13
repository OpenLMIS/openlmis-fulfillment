package org.openlmis.fulfillment.service.referencedata;

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
