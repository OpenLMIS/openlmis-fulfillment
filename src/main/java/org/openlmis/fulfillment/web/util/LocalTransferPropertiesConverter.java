package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.service.ExporterBuilder;

final class LocalTransferPropertiesConverter
    implements TransferPropertiesConverter<LocalTransferProperties, LocalTransferPropertiesDto> {

  @Override
  public boolean supports(Class clazz) {
    return LocalTransferProperties.class.equals(clazz)
        || LocalTransferPropertiesDto.class.equals(clazz);
  }

  @Override
  public LocalTransferProperties toDomain(LocalTransferPropertiesDto dto) {
    return LocalTransferProperties.newInstance(dto);
  }

  @Override
  public LocalTransferPropertiesDto toDto(LocalTransferProperties domain,
                                          ExporterBuilder exporter) {
    LocalTransferPropertiesDto dto = new LocalTransferPropertiesDto();
    exporter.export(domain, dto);

    return dto;
  }

}
