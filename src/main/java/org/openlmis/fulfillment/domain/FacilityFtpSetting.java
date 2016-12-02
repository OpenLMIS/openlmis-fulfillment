package org.openlmis.fulfillment.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class FacilityFtpSetting extends BaseEntity {
  private static final String TEXT_COLUMN_DEFINITION = "text";

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

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
  private String removeDirectory;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String localDirectory;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean passiveMode;

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
    this.removeDirectory = setting.removeDirectory;
    this.localDirectory = setting.localDirectory;
    this.passiveMode = setting.passiveMode;
  }
}
