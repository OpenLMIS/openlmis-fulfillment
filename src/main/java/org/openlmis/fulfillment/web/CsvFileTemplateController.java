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

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_FILE_TEMPLATE_CREATION;

import javax.validation.Valid;

import org.openlmis.fulfillment.domain.CsvFileTemplate;
import org.openlmis.fulfillment.domain.CsvTemplateType;
import org.openlmis.fulfillment.repository.CsvFileTemplateRepository;
import org.openlmis.fulfillment.service.CsvFileTemplateService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.web.util.CsvFileTemplateDto;
import org.openlmis.fulfillment.web.validator.CsvFileTemplateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Transactional
public class CsvFileTemplateController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CsvFileTemplateController.class);

  @Autowired
  private CsvFileTemplateValidator validator;
  @Autowired
  private CsvFileTemplateRepository csvFileTemplateRepository;
  @Autowired
  private CsvFileTemplateService csvFileTemplateService;
  @Autowired
  private PermissionService permissionService;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  /**
   * Allows updating order file templates.
   *
   * @param csvFileTemplateDto An order file template bound to the request body
   * @return ResponseEntity containing saved csvFileTemplate
   */
  @RequestMapping(value = "/csvFileTemplates", method = RequestMethod.PUT)
  @ResponseBody
  public CsvFileTemplateDto saveCsvFileTemplate(
          @RequestBody @Valid CsvFileTemplateDto csvFileTemplateDto, BindingResult bindingResult) {
    LOGGER.debug("Checking right to update order file template");
    permissionService.canManageSystemSettings();

    if (bindingResult.hasErrors()) {
      throw new ValidationException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }

    CsvFileTemplate template = csvFileTemplateService.getCsvFileTemplate(csvFileTemplateDto
        .getTemplateType());
    if (!template.getId().equals(csvFileTemplateDto.getId())) {
      throw new ValidationException(ERROR_ORDER_FILE_TEMPLATE_CREATION);
    }

    LOGGER.debug("Saving CSV File Template");
    template.importDto(csvFileTemplateDto);
    template = csvFileTemplateRepository.save(template);

    LOGGER.debug("Saved CSV File Template with id: " + template.getId());
    return CsvFileTemplateDto.newInstance(template);
  }

  /**
   * Get csvFileTemplate.
   *
   * @return CsvFileTemplate.
   */
  @RequestMapping(value = "/csvFileTemplates", method = RequestMethod.GET)
  public ResponseEntity<CsvFileTemplateDto> getCsvFileTemplate(
      @RequestParam(name = "templateType", required = false, defaultValue = "ORDER")
          CsvTemplateType templateType) {

    LOGGER.debug("Checking right to view order file template");
    permissionService.canManageSystemSettings();

    CsvFileTemplate csvFileTemplate = csvFileTemplateService.getCsvFileTemplate(templateType);
    if (csvFileTemplate == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(CsvFileTemplateDto.newInstance(csvFileTemplate),
          HttpStatus.OK);
    }
  }
}
