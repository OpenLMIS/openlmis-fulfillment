package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.openlmis.fulfillment.service.FacilityFtpSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Controller
public class FacilityFtpSettingController extends BaseController {

  @Autowired
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Autowired
  private FacilityFtpSettingService facilityFtpSettingService;

  /**
   * Allows creating new facility ftp settings.
   * If the id is specified, it will be ignored.
   *
   * @param setting A facility ftp setting bound to the request body
   * @return ResponseEntity containing the created facility ftp setting
   */
  @RequestMapping(value = "/facilityFtpSettings", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public FacilityFtpSetting createSetting(@RequestBody FacilityFtpSetting setting) {
    setting.setId(null);
    FacilityFtpSetting createdSetting = facilityFtpSettingService.save(setting);

    return createdSetting;
  }

  /**
   * Allows updating facility ftp settings.
   *
   * @param setting A facility ftp setting bound to the request body
   * @param settingId UUID of facility ftp setting which we want to update
   * @return ResponseEntity containing the updated facility ftp setting
   */
  @RequestMapping(value = "/facilityFtpSettings/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityFtpSetting updateSetting(@RequestBody FacilityFtpSetting setting,
                           @PathVariable("id") UUID settingId) {

    FacilityFtpSetting existent = facilityFtpSettingRepository.findOne(settingId);
    if (existent == null) {
      existent = new FacilityFtpSetting();
    }

    existent.updateFrom(setting);
    existent = facilityFtpSettingRepository.save(existent);

    return existent;
  }

  /**
   * Get chosen facility ftp setting.
   *
   * @param settingId UUID of facility ftp setting whose we want to get
   * @return FacilityFtpSetting.
   */
  @RequestMapping(value = "/facilityFtpSettings/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getSetting(@PathVariable("id") UUID settingId) {
    FacilityFtpSetting setting = facilityFtpSettingRepository.findOne(settingId);
    if (setting == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(setting, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting facility ftp setting.
   *
   * @param settingId UUID of facility ftp setting which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityFtpSettings/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSetting(@PathVariable("id") UUID settingId) {
    FacilityFtpSetting setting = facilityFtpSettingRepository.findOne(settingId);
    if (setting == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      facilityFtpSettingRepository.delete(setting);
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }
}
