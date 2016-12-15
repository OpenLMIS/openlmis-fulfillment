package org.openlmis.fulfillment.web.util;


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

  /**
   * Create new instance of OrderNumberConfigurationDto based on given
   * {@link OrderNumberConfiguration}
   * @param orderNumberConfiguration instance of OrderNumberConfiguration
   * @return new instance of OrderNumberConfigurationDto.
   */
  public static OrderNumberConfigurationDto newInstance(
      OrderNumberConfiguration orderNumberConfiguration) {
    OrderNumberConfigurationDto orderNumberConfigurationDto = new OrderNumberConfigurationDto();
    orderNumberConfiguration.export(orderNumberConfigurationDto);
    return orderNumberConfigurationDto;
  }
}
