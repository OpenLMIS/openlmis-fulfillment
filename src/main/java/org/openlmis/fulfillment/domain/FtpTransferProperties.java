package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

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

    Optional.ofNullable(importer.getFacility())
        .ifPresent(facility -> ftp.facilityId = facility.getId());

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

  public interface Exporter extends BaseExporter {

    void setProtocol(String protocol);

    void setUsername(String username);

    void setServerHost(String serverHost);

    void setServerPort(Integer serverPort);

    void setRemoteDirectory(String remoteDirectory);

    void setLocalDirectory(String localDirectory);

    void setPassiveMode(Boolean passiveMode);

  }

  public interface Importer extends BaseImporter {

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
