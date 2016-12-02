package org.openlmis.fulfillment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.annotations.Type;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "facility_ftp_settings")
@NoArgsConstructor
public class FacilityFtpSetting extends BaseEntity {
  @Column(nullable = false, unique = true)
  @Getter
  @Setter
  @Type(type = "pg-uuid")
  private UUID facilityId;

  @Column(nullable = false)
  @Getter
  @Setter
  private String serverHost;

  @Column(nullable = false)
  @Getter
  @Setter
  private String serverPort;

  @Column(nullable = false)
  @Getter
  @Setter
  private String path;

  @Column(nullable = false)
  @Getter
  @Setter
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(nullable = false)
  @Getter
  @Setter
  private String password;

  /**
   * Copy values of attributes into new or updated Order.
   *
   * @param setting setting with new values.
   */
  public void updateFrom(FacilityFtpSetting setting) {
    this.facilityId = setting.facilityId;
    this.serverHost = setting.serverHost;
    this.serverPort = setting.serverPort;
    this.path = setting.path;
    this.username = setting.username;
    this.password = setting.password;
  }
}
