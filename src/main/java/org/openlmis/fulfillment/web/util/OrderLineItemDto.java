package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.referencedata.OrderableProductDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItemDto implements OrderLineItem.Importer, OrderLineItem.Exporter {

  private UUID id;
  private OrderableProductDto orderableProduct;
  private Long orderedQuantity;
  private Long filledQuantity;
  private Long approvedQuantity;

  /**
   * Create new instance of TemplateParameterDto based on given {@link OrderLineItem}
   * @param line instance of Template
   * @return new instance of TemplateDto.
   */
  public static OrderLineItemDto newInstance(OrderLineItem line, ExporterBuilder exporter) {
    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    exporter.export(line, orderLineItemDto);

    return orderLineItemDto;
  }
}
