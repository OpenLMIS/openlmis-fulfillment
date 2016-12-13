package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.FtpTransferProperties;

import lombok.Getter;
import lombok.Setter;

public class FtpTransferPropertiesDto extends TransferPropertiesDto
    implements FtpTransferProperties.Importer, FtpTransferProperties.Exporter {

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

}
