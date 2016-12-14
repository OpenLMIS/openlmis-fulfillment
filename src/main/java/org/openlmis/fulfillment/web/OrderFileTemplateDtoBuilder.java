package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.dto.OrderFileColumnDto;
import org.openlmis.fulfillment.dto.OrderFileTemplateDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderFileTemplateDtoBuilder {

  /**
   * Create new list of OrderFileTemplateDto based on given list of {@link OrderFileTemplate}
   * @param templates instance of OrderFileTemplate
   * @return new instance of OrderFileTemplateDto.
   */
  public Iterable<OrderFileTemplateDto> build(Iterable<OrderFileTemplate> templates) {

    List<OrderFileTemplateDto> orderFileTemplateDtos = new ArrayList<>();
    for (OrderFileTemplate orderFileTemplate: templates) {
      orderFileTemplateDtos.add(build(orderFileTemplate));
    }
    return orderFileTemplateDtos;
  }

  /**
   * Create new instance of OrderFileTemplate based on given {@link OrderFileTemplate}
   * @param orderFileTemplate instance of OrderFileTemplate
   * @return new instance of OrderFileTemplateDto.
   */
  public OrderFileTemplateDto build(OrderFileTemplate orderFileTemplate) {
    if (orderFileTemplate == null) {
      return null;
    }
    OrderFileTemplateDto orderFileTemplateDto = new OrderFileTemplateDto();
    orderFileTemplate.export(orderFileTemplateDto);

    if (orderFileTemplate.getOrderFileColumns() != null) {
      for (OrderFileColumn orderFileColumn : orderFileTemplate.getOrderFileColumns()) {
        OrderFileColumnDto orderFileColumnDto = new OrderFileColumnDto();
        orderFileColumn.export(orderFileColumnDto);
        orderFileTemplateDto.getOrderFileColumns().add(orderFileColumnDto);
      }
    }
    return orderFileTemplateDto;
  }
}
