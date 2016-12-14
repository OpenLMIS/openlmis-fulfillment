package org.openlmis.fulfillment.web.util;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.openlmis.fulfillment.domain.TransferProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FtpTransferPropertiesDto.class, name = "ftp"),
    @JsonSubTypes.Type(value = LocalTransferPropertiesDto.class, name = "local")})
public abstract class TransferPropertiesDto
    implements TransferProperties.Importer, TransferProperties.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private UUID facilityId;

}
