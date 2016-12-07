package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "facility_ftp_settings")
@NoArgsConstructor
public class FacilityFtpSetting extends BaseEntity {

  @Column(nullable = false, unique = true)
  @Getter
  @Setter
  private UUID facilityId;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String protocol;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String username;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String password;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String serverHost;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer serverPort;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String remoteDirectory;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String localDirectory;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean passiveMode;

  /**
   * Creates a new instance of {@link FacilityFtpSetting} based on data from {@link Importer}.
   *
   * @param importer instance that implement {@link Importer}
   * @return an instance of {@link FacilityFtpSetting}
   */
  public static FacilityFtpSetting newInstance(Importer importer) {
    FacilityFtpSetting setting = new FacilityFtpSetting();
    setting.id = importer.getId();
    setting.facilityId = importer.getFacilityId();
    setting.protocol = importer.getProtocol();
    setting.username = importer.getUsername();
    setting.password = importer.getPassword();
    setting.serverHost = importer.getServerHost();
    setting.serverPort = importer.getServerPort();
    setting.remoteDirectory = importer.getRemoteDirectory();
    setting.localDirectory = importer.getLocalDirectory();
    setting.passiveMode = importer.isPassiveMode();

    return setting;
  }

  /**
   * Copy values of attributes into new or updated Facility FTP Setting.
   *
   * @param setting setting with new values.
   */
  public void updateFrom(FacilityFtpSetting setting) {
    this.facilityId = setting.facilityId;
    this.protocol = setting.protocol;
    this.username = setting.username;
    this.password = setting.password;
    this.serverHost = setting.serverHost;
    this.serverPort = setting.serverPort;
    this.remoteDirectory = setting.remoteDirectory;
    this.localDirectory = setting.localDirectory;
    this.passiveMode = setting.passiveMode;
  }

  /**
   * Exports current data from this Facility FTP Setting.
   *
   * @param exporter instance that implement {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setFacilityId(facilityId);
    exporter.setProtocol(protocol);
    exporter.setUsername(username);
    exporter.setServerHost(serverHost);
    exporter.setServerPort(serverPort);
    exporter.setRemoteDirectory(remoteDirectory);
    exporter.setLocalDirectory(localDirectory);
    exporter.setPassiveMode(passiveMode);
  }

  public interface Exporter {

    void setId(UUID id);

    void setFacilityId(UUID facilityId);

    void setProtocol(String protocol);

    void setUsername(String username);

    void setServerHost(String serverHost);

    void setServerPort(Integer serverPort);

    void setRemoteDirectory(String remoteDirectory);

    void setLocalDirectory(String localDirectory);

    void setPassiveMode(boolean passiveMode);

  }

  public interface Importer {

    UUID getId();

    UUID getFacilityId();

    String getProtocol();

    String getUsername();

    String getPassword();

    String getServerHost();

    Integer getServerPort();

    String getRemoteDirectory();

    String getLocalDirectory();

    boolean isPassiveMode();

  }
}
