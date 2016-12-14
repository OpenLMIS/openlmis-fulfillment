package org.openlmis.fulfillment.domain;


public class ProofOfDeliveryLineItemBuilder {

  /**
   * Create new instance of ProofOfDeliveryLineItem based on given
   * {@link ProofOfDeliveryLineItem.Importer}
   * @param importer instance of {@link ProofOfDeliveryLineItem.Importer}
   * @return instance of ProofOfDeliveryLineItem.
   */
  public static ProofOfDeliveryLineItem newProofOfDeliveryLineItem(
      ProofOfDeliveryLineItem.Importer importer) {
    ProofOfDeliveryLineItem proofOfDeliveryLineItem = new ProofOfDeliveryLineItem();

    proofOfDeliveryLineItem.setId(importer.getId());
    proofOfDeliveryLineItem.setOrderLineItem(OrderLineItem.newOrderLineItem(importer
        .getOrderLineItem()));
    proofOfDeliveryLineItem.setPackToShip(importer.getPackToShip());
    proofOfDeliveryLineItem.setQuantityShipped(importer.getQuantityShipped());
    proofOfDeliveryLineItem.setQuantityReceived(importer.getQuantityReceived());
    proofOfDeliveryLineItem.setQuantityReturned(importer.getQuantityReturned());
    proofOfDeliveryLineItem.setReplacedProductCode(importer.getReplacedProductCode());
    proofOfDeliveryLineItem.setNotes(importer.getNotes());

    return proofOfDeliveryLineItem;
  }
}
