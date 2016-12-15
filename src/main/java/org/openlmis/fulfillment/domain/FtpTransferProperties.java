package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("ftp")
@NoArgsConstructor
public class FtpTransferProperties extends TransferProperties {

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private FtpProtocol protocol;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String username;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String password;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String serverHost;

  @Column
  @Getter
  @Setter
  private Integer serverPort;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String remoteDirectory;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String localDirectory;

  @Column
  @Getter
  @Setter
  private Boolean passiveMode;

  @Override
  @Transient
  public String getPath() {
    return localDirectory;
  }

  /**
   * Creates a new instance of {@link FtpTransferProperties} based on data from {@link Importer}.
   *
   * @param importer instance that implement {@link Importer}
   * @return an instance of {@link FtpTransferProperties}
   */
  public static FtpTransferProperties newInstance(Importer importer) {
    FtpTransferProperties ftp = new FtpTransferProperties();
    ftp.id = importer.getId();
    ftp.facilityId = importer.getFacilityId();
    ftp.protocol = FtpProtocol.fromString(importer.getProtocol());
    ftp.username = importer.getUsername();
    ftp.password = importer.getPassword();
    ftp.serverHost = importer.getServerHost();
    ftp.serverPort = importer.getServerPort();
    ftp.remoteDirectory = importer.getRemoteDirectory();
    ftp.localDirectory = importer.getLocalDirectory();
    ftp.passiveMode = importer.getPassiveMode();

    return ftp;
  }

  /**
   * Exports current data from this Ftp Transfer Properties.
   *
   * @param exporter instance that implement {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setFacilityId(facilityId);
    exporter.setProtocol(protocol.name());
    exporter.setUsername(username);
    exporter.setServerHost(serverHost);
    exporter.setServerPort(serverPort);
    exporter.setRemoteDirectory(remoteDirectory);
    exporter.setLocalDirectory(localDirectory);
    exporter.setPassiveMode(passiveMode);
  }

  public interface Exporter extends TransferProperties.Exporter {

    void setProtocol(String protocol);

    void setUsername(String username);

    void setServerHost(String serverHost);

    void setServerPort(Integer serverPort);

    void setRemoteDirectory(String remoteDirectory);

    void setLocalDirectory(String localDirectory);

    void setPassiveMode(Boolean passiveMode);

  }

  public interface Importer extends TransferProperties.Importer {

    String getProtocol();

    String getUsername();

    String getPassword();

    String getServerHost();

    Integer getServerPort();

    String getRemoteDirectory();

    String getLocalDirectory();

    Boolean getPassiveMode();

  }

}
