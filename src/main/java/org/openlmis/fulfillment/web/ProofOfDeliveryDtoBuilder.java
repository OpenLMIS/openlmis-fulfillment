package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.dto.ProofOfDeliveryDto;
import org.openlmis.fulfillment.dto.ProofOfDeliveryLineItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class ProofOfDeliveryDtoBuilder {

  @Autowired
  OrderDtoBuilder orderDtoBuilder;

  @Autowired
  ProofOfDeliveryLineItemDtoBuilder proofOfDeliveryLineItemDtoBuilder;

  /**
   * Create new list of ProofOfDeliveryDto based on given list of {@link ProofOfDelivery}
   * @param proofOfDeliveries instance of ProofOfDelivery
   * @return new instance ProofOfDeliveryDto.
   */
  public Collection<ProofOfDeliveryDto> build(Iterable<ProofOfDelivery> proofOfDeliveries) {

    Collection<ProofOfDeliveryDto> proofOfDeliveries1 = new ArrayList<>();
    for (ProofOfDelivery proofOfDelivery: proofOfDeliveries) {
      proofOfDeliveries1.add(build(proofOfDelivery));
    }
    return proofOfDeliveries1;
  }

  /**
   * Create new instance of ProofOfDelivery based on given {@link ProofOfDelivery}
   * @param proofOfDelivery instance of ProofOfDelivery
   * @return new instance od ProofOfDeliveryDto.
   */
  public ProofOfDeliveryDto build(ProofOfDelivery proofOfDelivery) {
    ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
    proofOfDelivery.export(proofOfDeliveryDto);

    proofOfDeliveryDto.setOrder(orderDtoBuilder.build(proofOfDelivery.getOrder()));

    if (proofOfDelivery.getProofOfDeliveryLineItems() != null) {
      proofOfDeliveryDto.setProofOfDeliveryLineItems(new ArrayList<>());
      for (ProofOfDeliveryLineItem proofOfDeliveryLineItem : proofOfDelivery
          .getProofOfDeliveryLineItems()) {
        ProofOfDeliveryLineItemDto proofOfDeliveryLineItemDto = proofOfDeliveryLineItemDtoBuilder
            .build(proofOfDeliveryLineItem);
        proofOfDeliveryDto.getProofOfDeliveryLineItems().add(proofOfDeliveryLineItemDto);
      }
    }
    return proofOfDeliveryDto;
  }
}
