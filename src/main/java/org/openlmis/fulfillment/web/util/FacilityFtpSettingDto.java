package org.openlmis.fulfillment.web.util;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class FacilityFtpSettingDto
    implements FacilityFtpSetting.Exporter, FacilityFtpSetting.Importer {

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
  private boolean passiveMode;

  public static FacilityFtpSettingDto newInstance(FacilityFtpSetting setting) {
    FacilityFtpSettingDto dto = new FacilityFtpSettingDto();
    setting.export(dto);

    return dto;
  }

}
