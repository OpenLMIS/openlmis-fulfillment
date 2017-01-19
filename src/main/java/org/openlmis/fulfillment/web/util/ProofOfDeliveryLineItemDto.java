package org.openlmis.fulfillment.web.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.service.ExporterBuilder;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProofOfDeliveryLineItemDto implements ProofOfDeliveryLineItem.Importer,
    ProofOfDeliveryLineItem.Exporter {

  private UUID id;
  private OrderLineItemDto orderLineItem;
  private Long packsToShip;
  private Long quantityShipped;
  private Long quantityReceived;
  private Long quantityReturned;
  private String replacedProductCode;
  private String notes;

  /**
   * Create new instance of ProofOfDeliveryLineItemDto based of given
   * {@link ProofOfDeliveryLineItem}
   * @param proofOfDeliveryLineItem instance of {@link ProofOfDeliveryLineItem}
   * @return new instance of ProofOfDeliveryLineItemDto.
   */
  public static ProofOfDeliveryLineItemDto newInstance(
      ProofOfDeliveryLineItem proofOfDeliveryLineItem, ExporterBuilder exporter) {

    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    exporter.export(proofOfDeliveryLineItem.getOrderLineItem(), orderLineItemDto);

    ProofOfDeliveryLineItemDto proofOfDeliveryLineItemDto = new ProofOfDeliveryLineItemDto();
    proofOfDeliveryLineItem.export(proofOfDeliveryLineItemDto);

    proofOfDeliveryLineItemDto.setOrderLineItem(orderLineItemDto);

    return proofOfDeliveryLineItemDto;
  }
}
