package org.openlmis.fulfillment.dto;


import org.openlmis.fulfillment.domain.OrderNumberConfiguration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderNumberConfigurationDto implements OrderNumberConfiguration.Importer,
    OrderNumberConfiguration.Exporter {
  private UUID id;
  private String orderNumberPrefix;
  private Boolean includeOrderNumberPrefix;
  private Boolean includeProgramCode;
  private Boolean includeTypeSuffix;
}
