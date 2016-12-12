package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class TransferPropertiesDto
    implements TransferProperties.Importer, TransferProperties.Exporter,
    FtpTransferProperties.Importer, FtpTransferProperties.Exporter,
    LocalTransferProperties.Importer, LocalTransferProperties.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private UUID facilityId;

  @Getter
  @Setter
  private String protocol;

  @Getter
  @Setter
  private String username;

  @Getter
  private String password;

  @Getter
  @Setter
  private String serverHost;

  @Getter
  @Setter
  private Integer serverPort;

  @Getter
  @Setter
  private String remoteDirectory;

  @Getter
  @Setter
  private String localDirectory;

  @Getter
  @Setter
  private Boolean passiveMode;

  @Getter
  @Setter
  private String path;

}
