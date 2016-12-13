package org.openlmis.fulfillment.web.util;

import com.google.common.collect.Lists;

import org.openlmis.fulfillment.domain.TransferProperties;

import java.util.List;

public final class TransferPropertiesFactory {
  private static final List<TransferPropertiesConverter> CONVERTERS = Lists.newArrayList(
      new FtpTransferPropertiesConverter(), new LocalTransferPropertiesConverter()
  );

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
    TransferPropertiesConverter converter = CONVERTERS
        .stream()
        .filter(c -> c.supports(dto.getClass()))
        .findFirst()
        .orElse(null);

    if (null == converter) {
      throw new IllegalArgumentException("The given dto type is not supported: " + dto.getClass());
    }

    return converter.toDomain(dto);
  }

  /**
   * Creates a new instance of {@link TransferPropertiesDto} based on data from domain class.
   *
   * @param domain an instance of {@link TransferProperties}.
   * @return new instance of {@link TransferPropertiesDto}.
   */
  public static TransferPropertiesDto newInstance(TransferProperties domain) {
    TransferPropertiesConverter converter = CONVERTERS
        .stream()
        .filter(c -> c.supports(domain.getClass()))
        .findFirst()
        .orElse(null);

    if (null == converter) {
      throw new IllegalArgumentException(
          "The given domain type is not supported: " + domain.getClass()
      );
    }

    return converter.toDto(domain);
  }

}
