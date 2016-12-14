package org.openlmis.fulfillment.dto;


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
}
