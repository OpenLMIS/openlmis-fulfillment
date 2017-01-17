package org.openlmis.fulfillment.web.util;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = FtpTransferPropertiesDto.class, name = "ftp"),
    @JsonSubTypes.Type(value = LocalTransferPropertiesDto.class, name = "local")})
public abstract class TransferPropertiesDto
    implements TransferProperties.BaseImporter, TransferProperties.BaseExporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private FacilityDto facility;

}
