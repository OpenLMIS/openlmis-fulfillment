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

import static org.openlmis.fulfillment.i18n.MessageKeys.PROOF_OF_DELIVERY_ALREADY_CONFIRMED;
import static org.openlmis.fulfillment.i18n.MessageKeys.REPORTING_TEMPLATE_NOT_FOUND;
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
import org.openlmis.fulfillment.web.shipment.ShipmentController;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDtoBuilder;
import org.openlmis.fulfillment.web.util.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
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
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ShipmentController.class);

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
    XLOGGER.entry(shipmentId, pageable, authentication);
    Profiler profiler = new Profiler("GET_PODS");
    profiler.setLogger(XLOGGER);

    Page<ProofOfDelivery> page;

    if (null == shipmentId) {
      profiler.start("GET_ALL_PODS");
      page = proofOfDeliveryRepository.findAll(pageable);
    } else {
      profiler.start("FIND_SHIPMENT_BY_ID");
      Shipment shipment = shipmentRepository.findOne(shipmentId);

      if (null == shipment) {
        profiler.stop().log();
        throw new ValidationException(SHIPMENT_NOT_FOUND, shipmentId.toString());
      }

      profiler.start("FIND_PODS_BY_SHIPMENT");
      page = proofOfDeliveryRepository.findByShipment(shipment, pageable);
    }

    canManagePod(authentication, profiler, page.getContent().toArray(new ProofOfDelivery[0]));

    profiler.start("BUILD_DTOS");
    List<ProofOfDeliveryDto> dto = dtoBuilder.build(page.getContent());

    profiler.start("BUILD_DTO_PAGE");
    Page<ProofOfDeliveryDto> dtoPage = Pagination.getPage(dto, pageable, page.getTotalElements());

    profiler.stop().log();
    XLOGGER.exit(dtoPage);

    return dtoPage;
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
    XLOGGER.entry(proofOfDeliveryId, dto, authentication);
    Profiler profiler = new Profiler("UPDATE_POD");
    profiler.setLogger(XLOGGER);

    ProofOfDelivery toUpdate = findProofOfDelivery(proofOfDeliveryId, profiler);

    canManagePod(authentication, profiler, toUpdate);
    LOGGER.debug("Updating proofOfDelivery with id: {}", proofOfDeliveryId);

    if (toUpdate.isConfirmed()) {
      profiler.stop().log();
      throw new ValidationException(PROOF_OF_DELIVERY_ALREADY_CONFIRMED);
    }

    profiler.start("CREATE_DOMAIN_FROM_DTO");
    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(dto);
    // we always update resource
    profiler.start("UPDATE_POD");
    toUpdate.updateFrom(proofOfDelivery);

    if (dto.getStatus() == ProofOfDeliveryStatus.CONFIRMED) {
      profiler.start("CONFIRM_POD");
      toUpdate.confirm();

      profiler.start("UPDATE_ORDER_STATUS_AND_SAVE");
      Order order = toUpdate.getShipment().getOrder();
      order.setStatus(OrderStatus.RECEIVED);

      orderRepository.save(order);
    }

    profiler.start("SAVE_POD");
    toUpdate = proofOfDeliveryRepository.save(toUpdate);

    LOGGER.debug("Saved proofOfDelivery with id: {}", proofOfDeliveryId);
    profiler.start("BUILD_DTO");
    ProofOfDeliveryDto response = dtoBuilder.build(toUpdate);

    profiler.stop().log();
    XLOGGER.exit(response);

    return response;
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
    XLOGGER.entry(id, authentication);
    Profiler profiler = new Profiler("GET_POD");
    profiler.setLogger(XLOGGER);

    ProofOfDelivery proofOfDelivery = findProofOfDelivery(id, profiler);

    canManagePod(authentication, profiler, proofOfDelivery);

    profiler.start("BUILD_DTO");
    ProofOfDeliveryDto response = dtoBuilder.build(proofOfDelivery);

    profiler.stop().log();
    XLOGGER.exit(response);

    return response;
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
    XLOGGER.entry(id, request, response, authentication);
    Profiler profiler = new Profiler("PRINT_POD");
    profiler.setLogger(XLOGGER);

    canManagePod(authentication, id, profiler);

    profiler.start("FIND_TEMPLATE_BY_NAME");
    Template podPrintTemplate = templateService.getByName(PRINT_POD);
    if (podPrintTemplate == null) {
      profiler.stop().log();
      throw new ValidationException(REPORTING_TEMPLATE_NOT_FOUND, PRINT_POD);
    }

    profiler.start("GENERATE_JASPER_VIEW");
    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + id + "'";
    params.put("pod_id", formatId);

    JasperReportsMultiFormatView jasperView =
        jasperReportsViewService.getJasperReportsView(podPrintTemplate, request);

    profiler.start("RENDER_JASPER_VIEW");
    jasperView.render(params, request, response);

    profiler.stop().log();
    XLOGGER.exit();
  }

  private ProofOfDelivery findProofOfDelivery(UUID id, Profiler profiler) {
    profiler.start("FIND_POD_BY_ID");
    ProofOfDelivery entity = proofOfDeliveryRepository.findOne(id);

    if (null == entity) {
      profiler.stop().log();
      throw new ProofOfDeliveryNotFoundException(id);
    }

    return entity;
  }

  private void canManagePod(OAuth2Authentication authentication, Profiler profiler,
                            ProofOfDelivery... pods) {
    if (!authentication.isClientOnly()) {
      profiler.start("CHECK_PERMISSION");
      for (ProofOfDelivery pod : pods) {
        LOGGER.debug("Checking rights to manage POD: {}", pod.getId());
        permissionService.canManagePod(pod);
      }
    }
  }

  private void canManagePod(OAuth2Authentication authentication, UUID id,
                            Profiler profiler) {
    if (!authentication.isClientOnly()) {
      LOGGER.debug("Checking rights to manage POD: {}", id);
      profiler.start("CHECK_PERMISSION");
      permissionService.canManagePod(id);
    }
  }
}
