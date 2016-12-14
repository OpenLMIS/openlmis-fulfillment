package org.openlmis.fulfillment.dto;

import org.openlmis.fulfillment.domain.OrderLineItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemDto implements OrderLineItem.Importer, OrderLineItem.Exporter {

  private UUID id;
  private UUID orderableProductId;
  private Long orderedQuantity;
  private Long filledQuantity;
  private Long approvedQuantity;
}
