package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.FtpTransferProperties;

final class FtpTransferPropertiesConverter
    implements TransferPropertiesConverter<FtpTransferProperties, FtpTransferPropertiesDto> {

  @Override
  public boolean supports(Class clazz) {
    return FtpTransferProperties.class.equals(clazz)
        || FtpTransferPropertiesDto.class.equals(clazz);
  }

  @Override
  public FtpTransferProperties toDomain(FtpTransferPropertiesDto dto) {
    return FtpTransferProperties.newInstance(dto);
  }

  @Override
  public FtpTransferPropertiesDto toDto(FtpTransferProperties domain) {
    FtpTransferPropertiesDto dto = new FtpTransferPropertiesDto();
    domain.export(dto);

    return dto;
  }

}
