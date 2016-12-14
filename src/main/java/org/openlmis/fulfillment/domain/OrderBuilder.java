package org.openlmis.fulfillment.domain;

import java.util.ArrayList;

public class OrderBuilder {

  /**
   * Create a new instance of Order based on data from {@link Order.Importer}
   *
   * @param importer instance of {@link Order.Importer}
   * @return new instance of requisition.
   */
  public static Order newOrder(Order.Importer importer) {
    Order order = new Order();
    order.setId(importer.getId());
    order.setExternalId(importer.getExternalId());
    order.setEmergency(importer.getEmergency());
    order.setFacilityId(importer.getFacilityId());
    order.setProgramId(importer.getProgramId());
    order.setRequestingFacilityId(importer.getRequestingFacilityId());
    order.setReceivingFacilityId(importer.getReceivingFacilityId());
    order.setSupplyingFacilityId(importer.getSupplyingFacilityId());
    order.setOrderCode(importer.getOrderCode());
    order.setStatus(importer.getStatus());
    order.setQuotedCost(importer.getQuotedCost());
    order.setProcessingPeriodId(importer.getProcessingPeriodId());
    order.setCreatedDate(importer.getCreatedDate());
    order.setCreatedById(importer.getCreatedById());
    order.setSupervisoryNodeId(importer.getSupervisoryNodeId());
    order.setSupplyLineId(importer.getSupplyLineId());
    order.setOrderLineItems(new ArrayList<>());
    
    if (importer.getOrderLineItems() != null) {
      for (OrderLineItem.Importer orderLineItem : importer.getOrderLineItems()) {
        OrderLineItem item = OrderLineItem.newOrderLineItem(orderLineItem);
        order.getOrderLineItems().add(item);
      }
    }
    return order;
  }
}
