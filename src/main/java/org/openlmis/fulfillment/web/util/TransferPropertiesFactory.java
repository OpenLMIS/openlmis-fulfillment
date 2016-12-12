package org.openlmis.fulfillment.web.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;

public final class TransferPropertiesFactory {

  private TransferPropertiesFactory() {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a new instance of {@link TransferProperties} based on data from dto.
   *
   * @param dto transfer properties.
   * @return new instance of {@link TransferProperties}.
   */
  public static TransferProperties newInstance(TransferPropertiesDto dto) {
    return isNotBlank(dto.getPath())
        ? LocalTransferProperties.newInstance(dto)
        : FtpTransferProperties.newInstance(dto);
  }

  /**
   * Creates a new instance of {@link TransferPropertiesDto} based on data from domain class.
   *
   * @param properties an instance of {@link TransferProperties}.
   * @return new instance of {@link TransferPropertiesDto}.
   */
  public static TransferPropertiesDto newInstance(TransferProperties properties) {
    TransferPropertiesDto dto = new TransferPropertiesDto();

    if (properties instanceof LocalTransferProperties) {
      ((LocalTransferProperties) properties).export(dto);
    } else if (properties instanceof FtpTransferProperties) {
      ((FtpTransferProperties) properties).export(dto);
    }

    return dto;

  }

}
