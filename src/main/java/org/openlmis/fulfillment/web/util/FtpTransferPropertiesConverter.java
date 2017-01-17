package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.service.ExporterBuilder;

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
  public FtpTransferPropertiesDto toDto(FtpTransferProperties domain,
                                        ExporterBuilder exporter) {
    FtpTransferPropertiesDto dto = new FtpTransferPropertiesDto();
    exporter.export(domain, dto);

    return dto;
  }

}
