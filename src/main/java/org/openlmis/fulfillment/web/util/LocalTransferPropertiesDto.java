package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.LocalTransferProperties;

import lombok.Getter;
import lombok.Setter;

public class LocalTransferPropertiesDto extends TransferPropertiesDto
    implements LocalTransferProperties.Importer, LocalTransferProperties.Exporter {

  @Getter
  @Setter
  private String path;

}
