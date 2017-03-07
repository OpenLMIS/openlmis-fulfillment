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

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PROOF_OF_DELIVERY_ALREADY_SUBMITTED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ReportUtils;
import org.openlmis.fulfillment.web.validator.ProofOfDeliveryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

@Controller
@Transactional
public class ProofOfDeliveryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProofOfDeliveryController.class);
  private static final String PRINT_POD = "Print POD";

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ExporterBuilder exporter;

  @Autowired
  private ProofOfDeliveryValidator validator;

  @Autowired
  private PermissionService permissionService;

  /**
   * Get all proofOfDeliveries.
   *
   * @return ProofOfDeliveries.
   */
  @RequestMapping(value = "/proofOfDeliveries", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Collection<ProofOfDeliveryDto>> getAllProofOfDeliveries(
      OAuth2Authentication authentication) {
    Iterable<ProofOfDelivery> proofOfDeliveries = proofOfDeliveryRepository.findAll();

    for (ProofOfDelivery proofOfDelivery : proofOfDeliveries) {
      canManagePod(authentication, proofOfDelivery.getId());
    }

    return new ResponseEntity<>(
        ProofOfDeliveryDto.newInstance(proofOfDeliveries, exporter),
        HttpStatus.OK
    );
  }

  /**
   * Allows updating proofOfDeliveries.
   *
   * @param proofOfDeliveryId UUID of proofOfDelivery which we want to update
   * @param dto               A proofOfDeliveryDto bound to the request body
   * @return ResponseEntity containing the updated proofOfDelivery
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.PUT)
  public ResponseEntity updateProofOfDelivery(@PathVariable("id") UUID proofOfDeliveryId,
                                              @RequestBody ProofOfDeliveryDto dto,
                                              OAuth2Authentication authentication) {
    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(dto);
    ProofOfDelivery proofOfDeliveryToUpdate =
        proofOfDeliveryRepository.findOne(proofOfDeliveryId);
    if (proofOfDeliveryToUpdate == null) {
      proofOfDeliveryToUpdate = new ProofOfDelivery();
      proofOfDeliveryToUpdate.setOrder(proofOfDelivery.getOrder());

      if (!authentication.isClientOnly()) {
        permissionService.canManagePod(proofOfDeliveryToUpdate);
      }
      LOGGER.debug("Creating new proofOfDelivery");
    } else {
      canManagePod(authentication, proofOfDeliveryId);
      LOGGER.debug("Updating proofOfDelivery with id: " + proofOfDeliveryId);
    }

    OrderStatus status = proofOfDeliveryToUpdate.getOrder().getStatus();
    if (status.equals(OrderStatus.RECEIVED)) {
      throw new ValidationException(ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED);
    }

    proofOfDeliveryToUpdate.updateFrom(proofOfDelivery);
    proofOfDeliveryToUpdate = proofOfDeliveryRepository.save(proofOfDeliveryToUpdate);

    LOGGER.debug("Saved proofOfDelivery with id: " + proofOfDeliveryToUpdate.getId());
    return new ResponseEntity<>(
        ProofOfDeliveryDto.newInstance(proofOfDeliveryToUpdate, exporter),
        HttpStatus.OK
    );
  }

  /**
   * Get chosen proofOfDelivery.
   *
   * @param id UUID of proofOfDelivery whose we want to get
   * @return ProofOfDelivery.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.GET)
  public ResponseEntity<ProofOfDeliveryDto> getProofOfDelivery(
      @PathVariable("id") UUID id,
      OAuth2Authentication authentication) {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(id);
    if (proofOfDelivery == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      canManagePod(authentication, id);
      return new ResponseEntity<>(
          ProofOfDeliveryDto.newInstance(proofOfDelivery, exporter),
          HttpStatus.OK
      );
    }
  }

  /**
   * Print to PDF Proof of Delivery.
   *
   * @param id The UUID of the ProofOfDelivery to print
   * @return ResponseEntity with the "#200 OK" HTTP response status and Pdf file on success, or
   *         ResponseEntity containing the error description status.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}/print", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView print(HttpServletRequest request, @PathVariable("id") UUID id,
                            OAuth2Authentication authentication) {
    canManagePod(authentication, id);

    Template podPrintTemplate = templateService.getByName(PRINT_POD);
    if (podPrintTemplate == null) {
      throw new ValidationException(ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME, PRINT_POD);
    }

    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + id + "'";
    params.put("pod_id", formatId);

    JasperReportsMultiFormatView jasperView =
        jasperReportsViewService.getJasperReportsView(podPrintTemplate, request);

    return new ModelAndView(jasperView, params);
  }

  /**
   * Submit a Proof of Delivery.
   *
   * @param id The UUID of the ProofOfDelivery to submit
   * @return ProofOfDelivery.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}/submit", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity submit(@PathVariable("id") UUID id,
                               OAuth2Authentication authentication) {
    ProofOfDelivery pod = proofOfDeliveryRepository.findOne(id);

    if (null == pod) {
      throw new ProofOfDeliveryNotFoundException(id);
    }

    canManagePod(authentication, id);
    List<Message.LocalizedMessage> errors = validator.validate(pod);

    if (!errors.isEmpty()) {
      return ResponseEntity.badRequest().body(errors);
    }

    Order order = pod.getOrder();

    if (OrderStatus.RECEIVED == order.getStatus()) {
      throw new ValidationException(ERROR_PROOF_OF_DELIVERY_ALREADY_SUBMITTED);
    }

    order.setStatus(OrderStatus.RECEIVED);
    orderRepository.save(order);

    return ResponseEntity.ok(ProofOfDeliveryDto.newInstance(pod, exporter));
  }

  private void canManagePod(OAuth2Authentication authentication, UUID id) {
    if (!authentication.isClientOnly()) {
      LOGGER.debug("Checking rights to manage POD: {}", id);
      permissionService.canManagePod(id);
    }
  }
}
