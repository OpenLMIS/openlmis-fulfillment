package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.openlmis.fulfillment.repository.ConfigurationSettingRepository;
import org.openlmis.fulfillment.service.ConfigurationSettingException;
import org.openlmis.fulfillment.service.ConfigurationSettingService;
import org.openlmis.fulfillment.web.util.ConfigurationSettingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class ConfigurationSettingController extends BaseController {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private ConfigurationSettingService service;

  @Autowired
  private ConfigurationSettingRepository configurationSettingRepository;

  /**
   * Get all configuration settings.
   *
   * @return Configuration settings.
   */
  @RequestMapping(value = "/configurationSetting", method = RequestMethod.GET)
  @ResponseBody
  public Iterable<ConfigurationSettingDto> retrieveAll() {
    return StreamSupport.stream(configurationSettingRepository.findAll().spliterator(), false)
        .map(ConfigurationSettingDto::newInstance)
        .collect(Collectors.toList());
  }

  /**
   * Allows updating configuration setting.
   *
   * @param dto A configuration setting bound to the request body.
   * @return a configuration setting with new value.
   */
  @RequestMapping(value = "/configurationSetting", method = RequestMethod.PUT)
  @ResponseBody
  public ConfigurationSettingDto update(@RequestBody ConfigurationSettingDto dto)
      throws ConfigurationSettingException {
    LOGGER.debug("Updating configuration setting with key: {}", dto.getKey());

    ConfigurationSetting update = service.update(ConfigurationSetting.newInstance(dto));

    LOGGER.debug("Updated configuration setting with key: {}", dto.getKey());

    return ConfigurationSettingDto.newInstance(update);
  }

}
