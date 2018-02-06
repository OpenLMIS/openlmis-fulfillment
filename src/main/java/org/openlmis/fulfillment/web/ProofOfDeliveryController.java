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
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME;
import static org.openlmis.fulfillment.i18n.MessageKeys.SHIPMENT_NOT_FOUND;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.util.Pagination;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDtoBuilder;
import org.openlmis.fulfillment.web.util.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
  private PermissionService permissionService;

  @Autowired
  private ProofOfDeliveryDtoBuilder dtoBuilder;

  @Autowired
  private ShipmentRepository shipmentRepository;

  /**
   * Get all proofOfDeliveries.
   *
   * @return ProofOfDeliveries.
   */
  @RequestMapping(value = "/proofOfDeliveries", method = RequestMethod.GET)
  @ResponseBody
  public Page<ProofOfDeliveryDto> getAllProofOfDeliveries(
      @RequestParam(required = false) UUID shipmentId,
      Pageable pageable,
      OAuth2Authentication authentication) {

    Page<ProofOfDelivery> page;

    if (null == shipmentId) {
      page = proofOfDeliveryRepository.findAll(pageable);
    } else {
      Shipment shipment = shipmentRepository.findOne(shipmentId);

      if (null == shipment) {
        throw new ValidationException(SHIPMENT_NOT_FOUND, shipmentId.toString());
      }

      page = proofOfDeliveryRepository.findByShipment(shipment, pageable);
    }

    for (ProofOfDelivery proofOfDelivery : page.getContent()) {
      canManagePod(authentication, proofOfDelivery.getId());
    }

    List<ProofOfDeliveryDto> dto = dtoBuilder.build(page.getContent());
    return Pagination.getPage(dto, pageable, page.getTotalElements());
  }

  /**
   * Allows updating proofOfDeliveries.
   *
   * @param proofOfDeliveryId UUID of proofOfDelivery which we want to update
   * @param dto               A proofOfDeliveryDto bound to the request body
   * @return ResponseEntity containing the updated proofOfDelivery
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.PUT)
  @ResponseBody
  public ProofOfDeliveryDto updateProofOfDelivery(@PathVariable("id") UUID proofOfDeliveryId,
                                                  @RequestBody ProofOfDeliveryDto dto,
                                                  OAuth2Authentication authentication) {
    ProofOfDelivery toUpdate = proofOfDeliveryRepository.findOne(proofOfDeliveryId);

    if (null == toUpdate) {
      throw new ProofOfDeliveryNotFoundException(proofOfDeliveryId);
    }

    canManagePod(authentication, proofOfDeliveryId);
    LOGGER.debug("Updating proofOfDelivery with id: {}", proofOfDeliveryId);

    if (toUpdate.isConfirmed()) {
      throw new ValidationException(ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED);
    }

    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(dto);
    // we always update resource
    toUpdate.updateFrom(proofOfDelivery);

    if (dto.getStatus() == ProofOfDeliveryStatus.CONFIRMED) {
      toUpdate.confirm();
      Order order = toUpdate.getShipment().getOrder();
      order.setStatus(OrderStatus.RECEIVED);

      orderRepository.save(order);
    }

    toUpdate = proofOfDeliveryRepository.save(toUpdate);

    LOGGER.debug("Saved proofOfDelivery with id: {}", proofOfDeliveryId);
    return dtoBuilder.build(toUpdate);
  }

  /**
   * Get chosen proofOfDelivery.
   *
   * @param id UUID of proofOfDelivery whose we want to get
   * @return ProofOfDelivery.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ProofOfDeliveryDto getProofOfDelivery(@PathVariable("id") UUID id,
                                               OAuth2Authentication authentication) {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(id);

    if (null == proofOfDelivery) {
      throw new ProofOfDeliveryNotFoundException(id);
    }

    canManagePod(authentication, id);
    return dtoBuilder.build(proofOfDelivery);
  }

  /**
   * Print to PDF Proof of Delivery.
   *
   * @param id The UUID of the ProofOfDelivery to print
   *
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}/print", method = RequestMethod.GET)
  public void print(
      @PathVariable("id") UUID id, HttpServletRequest request, HttpServletResponse response,
      OAuth2Authentication authentication) throws Exception {
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

    jasperView.render(params, request, response);
  }

  private void canManagePod(OAuth2Authentication authentication, UUID id) {
    if (!authentication.isClientOnly()) {
      LOGGER.debug("Checking rights to manage POD: {}", id);
      permissionService.canManagePod(id);
    }
  }
}
