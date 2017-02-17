/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.openlmis.fulfillment.repository.ConfigurationSettingRepository;
import org.openlmis.fulfillment.service.ConfigurationSettingService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.web.util.ConfigurationSettingDto;
import org.openlmis.fulfillment.web.validator.ConfigurationSettingValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

@Controller
@Transactional
public class ConfigurationSettingController extends BaseController {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private ConfigurationSettingService service;

  @Autowired
  private ConfigurationSettingRepository configurationSettingRepository;

  @Autowired
  private ConfigurationSettingValidator validator;

  @Autowired
  private PermissionService permissionService;

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    binder.addValidators(validator);
  }

  /**
   * Get all configuration settings.
   *
   * @return Configuration settings.
   */
  @RequestMapping(value = "/configurationSettings", method = RequestMethod.GET)
  @ResponseBody
  public Iterable<ConfigurationSettingDto> retrieveAll() {

    LOGGER.debug("Checking rights to view Configuration Settings");
    permissionService.canManageSystemSettings();

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
  @RequestMapping(value = "/configurationSettings", method = RequestMethod.PUT)
  @ResponseBody
  public ConfigurationSettingDto update(@RequestBody @Valid ConfigurationSettingDto dto) {

    LOGGER.debug("Checking rights to update Configuration Settings");
    permissionService.canManageSystemSettings();

    LOGGER.debug("Updating configuration setting with key: {}", dto.getKey());

    ConfigurationSetting update = service.update(ConfigurationSetting.newInstance(dto));

    LOGGER.debug("Updated configuration setting with key: {}", dto.getKey());

    return ConfigurationSettingDto.newInstance(update);
  }

}
