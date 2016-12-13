package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.TransferProperties;

interface TransferPropertiesConverter
    <A extends TransferProperties, B extends TransferPropertiesDto> {

  boolean supports(Class clazz);

  A toDomain(B dto);

  B toDto(A domain);

}
