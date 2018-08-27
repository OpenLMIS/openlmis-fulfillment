/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.fulfillment.web.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;

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
   * Create new list of OrderFileTemplateDto based on given list of {@link OrderFileTemplate}.
   * @param templates instance of OrderFileTemplate
   * @return new instance of OrderFileTemplateDto.
   */
  public static Iterable<OrderFileTemplateDto> newInstance(Iterable<OrderFileTemplate> templates) {

    List<OrderFileTemplateDto> orderFileTemplateDtos = new ArrayList<>();
    templates.forEach(t -> orderFileTemplateDtos.add(newInstance(t)));
    return orderFileTemplateDtos;
  }

  /**
   * Create new instance of OrderFileTemplateDto based on given {@link OrderFileTemplate}.
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
