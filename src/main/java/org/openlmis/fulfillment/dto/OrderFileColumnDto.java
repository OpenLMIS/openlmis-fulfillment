package org.openlmis.fulfillment.dto;

import org.openlmis.fulfillment.domain.OrderFileColumn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFileColumnDto implements OrderFileColumn.Importer, OrderFileColumn.Exporter {

  private UUID id;
  private Boolean openLmisField;
  private String dataFieldLabel;
  private String columnLabel;
  private Boolean include;
  private Integer position;
  private String format;
  private String nested;
  private String keyPath;
  private String related;
  private String relatedKeyPath;
}
