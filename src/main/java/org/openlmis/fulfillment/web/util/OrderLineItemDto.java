package org.openlmis.fulfillment.web.util;

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

  /**
   * Create new instance of TemplateParameterDto based on given {@link OrderLineItem}
   * @param orderLineItem instance of Template
   * @return new instance of TemplateDto.
   */
  public static OrderLineItemDto newInstance(OrderLineItem orderLineItem) {
    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    orderLineItem.export(orderLineItemDto);
    return orderLineItemDto;
  }
}
