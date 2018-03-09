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
import static org.openlmis.fulfillment.i18n.MessageKeys.SHIPMENT_NOT_FOUND;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;
import static org.openlmis.fulfillment.service.PermissionService.PODS_VIEW;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.FulfillmentNotificationService;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.service.referencedata.PermissionStringDto;
import org.openlmis.fulfillment.service.referencedata.PermissionStrings;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.util.Pagination;
import org.openlmis.fulfillment.web.shipment.ShipmentController;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDtoBuilder;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

@Controller
@Transactional
public class ProofOfDeliveryController extends BaseController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProofOfDeliveryController.class);
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ShipmentController.class);

  private static final String CHECK_PERMISSION = "CHECK_PERMISSION";

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

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private DateHelper dateHelper;

  @Autowired
  private StockEventBuilder stockEventBuilder;

  @Autowired
  private StockEventStockManagementService stockEventStockManagementService;

  @Autowired
  private FulfillmentNotificationService fulfillmentNotificationService;

  /**
   * Get all proofs of delivery.
   *
   * @return proofs of delivery.
   */
  @RequestMapping(value = "/proofsOfDelivery", method = RequestMethod.GET)
  @ResponseBody
  public Page<ProofOfDeliveryDto> getAllProofsOfDelivery(
      @RequestParam(required = false) UUID shipmentId,
      Pageable pageable) {
    XLOGGER.entry(shipmentId, pageable);
    Profiler profiler = new Profiler("GET_PODS");
    profiler.setLogger(XLOGGER);

    List<ProofOfDelivery> content;

    if (null == shipmentId) {
      profiler.start("GET_ALL_PODS");
      content = proofOfDeliveryRepository.findAll();
    } else {
      profiler.start("FIND_SHIPMENT_BY_ID");
      Shipment shipment = shipmentRepository.findOne(shipmentId);

      if (null == shipment) {
        profiler.stop().log();
        throw new ValidationException(SHIPMENT_NOT_FOUND, shipmentId.toString());
      }

      profiler.start("FIND_PODS_BY_SHIPMENT");
      content = proofOfDeliveryRepository.findByShipment(shipment);
    }

    UserDto user = authenticationHelper.getCurrentUser();

    if (null != user) {
      profiler.start(CHECK_PERMISSION);
      PermissionStrings.Handler handler = permissionService.getPermissionStrings(user.getId());
      Set<PermissionStringDto> permissionStrings = handler.get();

      content.removeIf(proofOfDelivery -> {
        UUID receivingFacilityId = proofOfDelivery.getReceivingFacilityId();
        UUID programId = proofOfDelivery.getProgramId();

        return permissionStrings
            .stream()
            .noneMatch(elem -> elem.match(PODS_MANAGE, receivingFacilityId, programId)
                || elem.match(PODS_VIEW, receivingFacilityId, programId));
      });
    }

    profiler.start("BUILD_DTOS");
    List<ProofOfDeliveryDto> dto = dtoBuilder.build(content);

    profiler.start("BUILD_DTO_PAGE");
    Page<ProofOfDeliveryDto> dtoPage = Pagination.getPage(dto, pageable);

    profiler.stop().log();
    XLOGGER.exit(dtoPage);

    return dtoPage;
  }

  /**
   * Allows updating proofs of delivery.
   *
   * @param proofOfDeliveryId UUID of proofOfDelivery which we want to update
   * @param dto               A proofOfDeliveryDto bound to the request body
   * @return ResponseEntity containing the updated proofOfDelivery
   */
  @RequestMapping(value = "/proofsOfDelivery/{id}", method = RequestMethod.PUT)
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
      order.updateStatus(OrderStatus.RECEIVED, new UpdateDetails(
          authenticationHelper.getCurrentUser().getId(),
          dateHelper.getCurrentDateTimeWithSystemZone()));

      orderRepository.save(order);

      profiler.start("SEND_STOCK_EVENT");
      StockEventDto event = stockEventBuilder.fromProofOfDelivery(toUpdate);
      stockEventStockManagementService.submit(event);

      fulfillmentNotificationService.sendPodConfirmedNotification(toUpdate);
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
  @RequestMapping(value = "/proofsOfDelivery/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ProofOfDeliveryDto getProofOfDelivery(@PathVariable("id") UUID id,
                                               @RequestParam(required = false) Set<String> expand,
                                               OAuth2Authentication authentication) {
    XLOGGER.entry(id, authentication);
    Profiler profiler = new Profiler("GET_POD");
    profiler.setLogger(XLOGGER);

    ProofOfDelivery proofOfDelivery = findProofOfDelivery(id, profiler);
    canViewPod(authentication, profiler, proofOfDelivery);

    profiler.start("BUILD_DTO");
    ProofOfDeliveryDto response = dtoBuilder.build(proofOfDelivery);
    expandDto(response, expand);

    profiler.stop().log();
    XLOGGER.exit(response);

    return response;
  }

  /**
   * Prints proofOfDelivery in PDF format.
   *
   * @param id UUID of ProofOfDelivery to print
   */
  @RequestMapping(value = "/proofsOfDelivery/{id}/print", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public ModelAndView printProofOfDelivery(HttpServletRequest request,
      @PathVariable("id") UUID id, OAuth2Authentication authentication) throws IOException {

    XLOGGER.entry(id, authentication);
    Profiler profiler = new Profiler("GET_POD");
    profiler.setLogger(XLOGGER);

    ProofOfDelivery proofOfDelivery = findProofOfDelivery(id, profiler);
    canViewPod(authentication, profiler, proofOfDelivery);

    profiler.start("LOAD_JASPER_TEMPLATE");
    String filePath = "jasperTemplates/proofOfDelivery.jrxml";
    ClassLoader classLoader = getClass().getClassLoader();

    Template template = new Template();
    template.setName("ordersJasperTemplate");

    try (InputStream fis = classLoader.getResourceAsStream(filePath)) {
      templateService.createTemplateParameters(template, fis);
    }
    profiler.start("GENERATE_JASPER_VIEW");
    JasperReportsMultiFormatView jasperView = jasperReportsViewService
        .getJasperReportsView(template, request);

    Map<String, Object> params = new HashMap<>();
    params.put("format", "pdf");
    params.put("id", proofOfDelivery.getId());

    ModelAndView modelAndView = new ModelAndView(jasperView, params);

    profiler.stop().log();
    XLOGGER.exit(modelAndView);

    return modelAndView;
  }

  /**
   * Get the audit information related to the given proof of delivery.
   *
   * @param author              The author of the changes which should be returned.
   *                            If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *                            If null or empty, changes associated with any and all properties
   *                            are returned.
   * @param page                A Pageable object that allows client to optionally add "page"
   *                            (page number) and "size" (page size) query parameters to the
   *                            request.
   */
  @RequestMapping(value = "proofsOfDelivery/{id}/auditLog", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseEntity<String> getAuditLog(
      @PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
          String changedPropertyName,
      Pageable page,
      OAuth2Authentication authentication) {
    Profiler profiler = new Profiler("GET_AUDIT_LOG");
    profiler.setLogger(LOGGER);

    ProofOfDelivery pod = findProofOfDelivery(id, profiler);
    canManagePod(authentication, profiler, pod);

    profiler.start("GET_AUDIT_LOG");
    ResponseEntity<String> response = getAuditLogResponse(
        ProofOfDelivery.class, id, author, changedPropertyName, page
    );

    profiler.stop().log();

    return response;
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
                            ProofOfDelivery pod) {
    if (!authentication.isClientOnly()) {
      profiler.start(CHECK_PERMISSION);
      LOGGER.debug("Checking rights to manage POD: {}", pod.getId());
      permissionService.canManagePod(pod);
    }
  }

  private void canViewPod(OAuth2Authentication authentication, Profiler profiler,
                          ProofOfDelivery pod) {
    if (!authentication.isClientOnly()) {
      profiler.start(CHECK_PERMISSION);
      LOGGER.debug("Checking rights to view POD: {}", pod.getId());
      permissionService.canViewPod(pod);
    }
  }
}
