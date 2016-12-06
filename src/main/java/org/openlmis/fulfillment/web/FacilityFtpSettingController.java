package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.openlmis.fulfillment.service.DuplicateFacilityFtpSettingException;
import org.openlmis.fulfillment.service.FacilityFtpSettingService;
import org.openlmis.fulfillment.web.util.FacilityFtpSettingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityFtpSettingController.class);

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
  public FacilityFtpSettingDto createSetting(@RequestBody FacilityFtpSettingDto setting)
      throws DuplicateFacilityFtpSettingException {
    LOGGER.debug("Creating new Facility Ftp Setting");

    setting.setId(null);
    FacilityFtpSetting saved = facilityFtpSettingService.save(
        FacilityFtpSetting.newInstance(setting)
    );

    LOGGER.debug("Created new Facility Ftp Setting with id: {}", saved.getId());

    return FacilityFtpSettingDto.newInstance(saved);
  }

  /**
   * Allows updating facility ftp settings.
   *
   * @param setting   A facility ftp setting bound to the request body
   * @param settingId UUID of facility ftp setting which we want to update
   * @return ResponseEntity containing the updated facility ftp setting
   */
  @RequestMapping(value = "/facilityFtpSettings/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public FacilityFtpSettingDto updateSetting(@RequestBody FacilityFtpSettingDto setting,
                                             @PathVariable("id") UUID settingId) {
    FacilityFtpSetting toUpdate = facilityFtpSettingRepository.findOne(settingId);

    if (toUpdate == null) {
      toUpdate = new FacilityFtpSetting();
      LOGGER.info("Creating new Facility Ftp Setting");
    } else {
      LOGGER.debug("Updating Facility Ftp Setting with id: {}", settingId);
    }

    toUpdate.updateFrom(FacilityFtpSetting.newInstance(setting));
    toUpdate = facilityFtpSettingRepository.save(toUpdate);

    LOGGER.debug("Saved Facility Ftp Setting with id: {}", toUpdate.getId());

    return FacilityFtpSettingDto.newInstance(toUpdate);
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
    return setting == null
        ? ResponseEntity.notFound().build()
        : ResponseEntity.ok(FacilityFtpSettingDto.newInstance(setting));
  }

  /**
   * Allows deleting facility ftp setting.
   *
   * @param settingId UUID of facility ftp setting which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/facilityFtpSettings/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteSetting(@PathVariable("id") UUID settingId) {
    FacilityFtpSetting toDelete = facilityFtpSettingRepository.findOne(settingId);

    if (toDelete == null) {
      return ResponseEntity.notFound().build();
    } else {
      facilityFtpSettingRepository.delete(toDelete);
      return ResponseEntity.noContent().build();
    }
  }
}
