package org.openlmis.fulfillment.domain;

import java.util.ArrayList;

public class ProofOfDeliveryBuilder {
  /**
   * Create instance of ProofOfDelivery based on given {@link ProofOfDelivery.Importer}
   * @param importer instance of {@link ProofOfDelivery.Importer}
   * @return instance of ProofOfDelivery.
   */

  public static ProofOfDelivery newProofOfDelivery(ProofOfDelivery.Importer importer) {
    ProofOfDelivery proofOfDelivery = new ProofOfDelivery();
    proofOfDelivery.setOrder(OrderBuilder.newOrder(importer.getOrder()));
    proofOfDelivery.setTotalShippedPacks(importer.getTotalShippedPacks());
    proofOfDelivery.setTotalReceivedPacks(importer.getTotalReceivedPacks());
    proofOfDelivery.setTotalReturnedPacks(importer.getTotalReturnedPacks());
    proofOfDelivery.setDeliveredBy(importer.getDeliveredBy());
    proofOfDelivery.setReceivedBy(importer.getReceivedBy());
    proofOfDelivery.setReceivedDate(importer.getReceivedDate());

    if (importer.getProofOfDeliveryLineItems() != null) {
      proofOfDelivery.setProofOfDeliveryLineItems(new ArrayList<>());
      for (ProofOfDeliveryLineItem.Importer proofOfDeliveryLineItem : importer
          .getProofOfDeliveryLineItems()) {
        proofOfDelivery.getProofOfDeliveryLineItems().add(
            ProofOfDeliveryLineItemBuilder.newProofOfDeliveryLineItem(proofOfDeliveryLineItem)
        );
      }
    }

    return proofOfDelivery;
  }
}
