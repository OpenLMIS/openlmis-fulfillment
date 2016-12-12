package org.openlmis.fulfillment.domain;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.Arrays;

public enum FtpProtocol {
  FTP, SFTP, FTPS;

  /**
   * Find correct FTP protocol by the given string.
   *
   * @param protocol string representation of FTP protocol.
   * @return {@link FtpProtocol} that is equal to the given string.
   */
  public static FtpProtocol fromString(String protocol) {
    return Arrays.stream(values())
        .filter(p -> equalsIgnoreCase(protocol, p.name()))
        .findFirst()
        .orElse(null);
  }
}
