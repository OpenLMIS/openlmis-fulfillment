package org.openlmis.fulfillment;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;

import java.util.UUID;

public class OrderLineItemDataBuilder {
  private UUID id = UUID.randomUUID();
  private Order order;
  private UUID orderableId = UUID.randomUUID();
  private Long orderedQuantity = 1200L;
  private Long filledQuantity = 0L;
  private Long packsToShip = 0L;

  public OrderLineItemDataBuilder withoutId() {
    id = null;
    return this;
  }

  public OrderLineItemDataBuilder withRandomOrderedQuantity() {
    orderedQuantity = (long) (Math.random() * (5000));
    return this;
  }

  public OrderLineItem build() {
    OrderLineItem lineItem = new OrderLineItem(
        order, orderableId, orderedQuantity, filledQuantity, packsToShip
    );
    lineItem.setId(id);

    return lineItem;
  }

}
