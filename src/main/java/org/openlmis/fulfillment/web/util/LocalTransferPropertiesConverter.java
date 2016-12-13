package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.LocalTransferProperties;

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
  public LocalTransferPropertiesDto toDto(LocalTransferProperties domain) {
    LocalTransferPropertiesDto dto = new LocalTransferPropertiesDto();
    domain.export(dto);

    return dto;
  }

}
