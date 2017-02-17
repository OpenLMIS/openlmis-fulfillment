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

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_IO;

import org.apache.commons.io.IOUtils;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ReportingException;
import org.openlmis.fulfillment.service.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

@Controller
@Transactional
public class ProofOfDeliveryTemplateController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFileTemplateController.class);


  private static final String PRINT_POD = "Print POD";
  private static final String DESCRIPTION_POD = "Template to print Proof Of Delivery";
  private static final String CONSISTENCY_REPORT = "Consistency Report";

  @Autowired
  private TemplateService templateService;

  @Autowired
  private PermissionService permissionService;

  /**
   * Add Proof Of Delivery report templates with ".jrxml" format(extension) to database.
   *
   * @param file File in ".jrxml" format to add or upload.
   */
  @RequestMapping(value = "/proofOfDeliveryTemplates", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void saveTemplateOfPod(@RequestPart("file") MultipartFile file) {

    LOGGER.debug("Checking right to create proof of delivery template");
    permissionService.canManageSystemSettings();

    Template template = new Template(PRINT_POD, null, null, CONSISTENCY_REPORT, DESCRIPTION_POD);
    templateService.validateFileAndSaveTemplate(template, file);
  }

  /**
   * Download report template with ".jrxml" format(extension) for Proof of Delivery from database.
   *
   * @param response HttpServletResponse object.
   */
  @RequestMapping(value = "/proofOfDeliveryTemplates", method = RequestMethod.GET)
  @ResponseBody
  public void downloadPodXmlTemlate(HttpServletResponse response)
      throws IOException {
    LOGGER.debug("Checking right to view proof of delivery template");
    permissionService.canManageSystemSettings();
    Template podPrintTemplate = templateService.getByName(PRINT_POD);
    if (podPrintTemplate == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND,
          "Proof Of Delivery template does not exist.");
    } else {
      response.setContentType("application/xml");
      response.addHeader("Content-Disposition", "attachment; filename=podPrint" + ".jrxml");

      File file = templateService.convertJasperToXml(podPrintTemplate);

      try (InputStream fis = new FileInputStream(file);
           InputStream bis = new BufferedInputStream(fis)) {

        IOUtils.copy(bis, response.getOutputStream());
        response.flushBuffer();
      } catch (IOException ex) {
        throw new ReportingException(ex, ERROR_IO, ex.getMessage());
      }
    }
  }
}
