package org.openlmis.fulfillment.web.util;


import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
public class OrderFileTemplateDto implements OrderFileTemplate.Importer,
    OrderFileTemplate.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private String filePrefix;

  @Getter
  @Setter
  private Boolean headerInFile;

  @Setter
  private List<OrderFileColumnDto> orderFileColumns;

  @Override
  public List<OrderFileColumn.Importer> getOrderFileColumns() {
    return new ArrayList<>(
        Optional.ofNullable(orderFileColumns).orElse(Collections.emptyList())
    );
  }

  /**
   * Create new list of OrderFileTemplateDto based on given list of {@link OrderFileTemplate}
   * @param templates instance of OrderFileTemplate
   * @return new instance of OrderFileTemplateDto.
   */
  public static Iterable<OrderFileTemplateDto> newInstance(Iterable<OrderFileTemplate> templates) {

    List<OrderFileTemplateDto> orderFileTemplateDtos = new ArrayList<>();
    templates.forEach(t -> orderFileTemplateDtos.add(newInstance(t)));
    return orderFileTemplateDtos;
  }

  /**
   * Create new instance of OrderFileTemplateDto based on given {@link OrderFileTemplate}
   * @param orderFileTemplate instance of OrderFileTemplate
   * @return new instance of OrderFileTemplateDto.
   */
  public static OrderFileTemplateDto newInstance(OrderFileTemplate orderFileTemplate) {
    OrderFileTemplateDto orderFileTemplateDto = new OrderFileTemplateDto();
    orderFileTemplate.export(orderFileTemplateDto);

    if (orderFileTemplate.getOrderFileColumns() != null) {
      orderFileTemplateDto.setOrderFileColumns(orderFileTemplate.getOrderFileColumns()
          .stream().map(OrderFileColumnDto::newInstance).collect(Collectors.toList()));
    }
    return orderFileTemplateDto;
  }
}
