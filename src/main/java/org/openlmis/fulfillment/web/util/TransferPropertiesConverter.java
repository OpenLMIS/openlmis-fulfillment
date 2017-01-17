package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.service.ExporterBuilder;

interface TransferPropertiesConverter
    <A extends TransferProperties, B extends TransferPropertiesDto> {

  boolean supports(Class clazz);

  A toDomain(B dto);

  B toDto(A domain, ExporterBuilder exporter);

}
