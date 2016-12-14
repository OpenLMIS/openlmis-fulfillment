package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.dto.OrderLineItemDto;
import org.openlmis.fulfillment.dto.ProofOfDeliveryLineItemDto;
import org.springframework.stereotype.Component;

@Component
public class ProofOfDeliveryLineItemDtoBuilder {

  /**
   * Create new instance of ProofOfDeliveryLineItemDto based of given
   * {@link ProofOfDeliveryLineItem}
   * @param proofOfDeliveryLineItem instance of {@link ProofOfDeliveryLineItem}
   * @return new instance of ProofOfDeliveryLineItemDto.
   */
  public ProofOfDeliveryLineItemDto build(ProofOfDeliveryLineItem proofOfDeliveryLineItem) {
    ProofOfDeliveryLineItemDto proofOfDeliveryLineItemDto = new ProofOfDeliveryLineItemDto();
    proofOfDeliveryLineItem.export(proofOfDeliveryLineItemDto);

    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    proofOfDeliveryLineItem.getOrderLineItem().export(orderLineItemDto);
    proofOfDeliveryLineItemDto.setOrderLineItem(orderLineItemDto);

    return proofOfDeliveryLineItemDto;
  }
}
