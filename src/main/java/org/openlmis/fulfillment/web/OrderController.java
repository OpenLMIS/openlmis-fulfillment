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

import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_RETRY_INVALID_STATUS;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.ObjReferenceExpander;
import org.openlmis.fulfillment.service.OrderCsvHelper;
import org.openlmis.fulfillment.service.OrderFileTemplateService;
import org.openlmis.fulfillment.service.OrderSearchParams;
import org.openlmis.fulfillment.service.OrderSecurityService;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.Pagination;
import org.openlmis.fulfillment.web.util.BasicOrderDto;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderPeriodFilter;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Transactional
public class OrderController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
  private static final String DISPOSITION_BASE = "attachment; filename=";

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderCsvHelper csvHelper;

  @Autowired
  private OrderFileTemplateService orderFileTemplateService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ExporterBuilder exporter;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private OrderSecurityService orderSecurityService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ObjReferenceExpander objReferenceExpander;

  /**
   * Allows creating new orders.
   * If the id is specified, it will be ignored.
   *
   * @param orderDto A order bound to the request body
   * @return the newly created order
   */
  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public OrderDto createOrder(@RequestBody OrderDto orderDto,
                              OAuth2Authentication authentication) {
    Order order = createSingleOrder(orderDto, authentication);
    return OrderDto.newInstance(order, exporter);
  }

  /**
   * Allows creating multiple new orders at once in a single transaction.
   * If the id is specified for any of the orders, it will be ignored.
   *
   * @param orders A list of orders to be created
   * @return a list of newly created orders
   */
  @RequestMapping(value = "/orders/batch", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public Iterable<BasicOrderDto> batchCreateOrders(@RequestBody List<OrderDto> orders,
                                                   OAuth2Authentication authentication) {
    List<Order> newOrders = orders
        .stream()
        .map(order -> createSingleOrder(order, authentication))
        .collect(Collectors.toList());
    return BasicOrderDto.newInstance(newOrders, exporter);
  }

  /**
   * Get all orders.
   *
   * @return OrderDtos.
   */
  @RequestMapping(value = "/orders", method = RequestMethod.GET)
  @ResponseBody
  public Iterable<BasicOrderDto> getAllOrders() {
    return BasicOrderDto.newInstance(orderRepository.findAll(), exporter);
  }

  /**
   * Get chosen order.
   *
   * @param orderId UUID of order whose we want to get
   * @param expand a set of field names to expand
   * @return OrderDto.
   */
  @RequestMapping(value = "/orders/{id}", method = RequestMethod.GET)
  @ResponseBody
  public OrderDto getOrder(@PathVariable("id") UUID orderId,
                           @RequestParam(required = false) Set<String> expand) {
    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      throw new OrderNotFoundException(orderId);
    } else {
      permissionService.canViewOrder(order);
      OrderDto orderDto = OrderDto.newInstance(order, exporter);
      objReferenceExpander.expandDto(orderDto, expand);
      return orderDto;
    }
  }

  /**
   * Finds Orders matching all of provided parameters.
   *
   * @param params provided parameters.
   * @return ResponseEntity with list of all Orders matching provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/orders/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<BasicOrderDto> searchOrders(OrderSearchParams params, Pageable pageable) {
    List<Order> orders = orderService.searchOrders(params);

    List<Order> filteredList = orderSecurityService.filterInaccessibleOrders(orders);
    List<BasicOrderDto> data = BasicOrderDto.newInstance(filteredList, exporter);
    List<BasicOrderDto> filteredData = data
        .stream()
        .filter(new OrderPeriodFilter(params.getPeriodStartDate(), params.getPeriodEndDate()))
        .collect(Collectors.toList());

    return Pagination.getPage(filteredData, pageable);
  }

  /**
   * Retrieves the distinct UUIDs of the available requesting facilities.
   */
  @RequestMapping(value = "/orders/requestingFacilities", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UUID> getRequestingFacilities(
      @RequestParam(name = "supplyingFacility", required = false) UUID supplyingFacility) {
    return orderRepository.getRequestingFacilities(supplyingFacility);
  }

  /**
   * Returns csv or pdf of defined object in response.
   *
   * @param orderId UUID of order to print
   * @param format  String describing return format (pdf or csv)
   */
  @RequestMapping(value = "/orders/{id}/print", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public ModelAndView printOrder(HttpServletRequest request,
                                 @PathVariable("id") UUID orderId,
                                 @RequestParam("format") String format) throws IOException {

    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      throw new OrderNotFoundException(orderId);
    }
    permissionService.canViewOrder(order);

    String filePath = "jasperTemplates/ordersJasperTemplate.jrxml";
    ClassLoader classLoader = getClass().getClassLoader();

    Template template = new Template();
    template.setName("ordersJasperTemplate");

    try (InputStream fis = classLoader.getResourceAsStream(filePath)) {
      templateService.createTemplateParameters(template, fis);
    }
    JasperReportsMultiFormatView jasperView = jasperReportsViewService
        .getJasperReportsView(template, request);

    Map<String, Object> params = new HashMap<>();
    params.put("format", format);

    return jasperReportsViewService.getOrderJasperReportView(jasperView, params, order);
  }


  /**
   * Exporting order to csv.
   *
   * @param orderId  UUID of order to print
   * @param type     export type
   * @param response HttpServletResponse object
   */
  @RequestMapping(value = "/orders/{id}/export", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public void export(@PathVariable("id") UUID orderId,
                  @RequestParam(value = "type", required = false, defaultValue = "csv") String type,
                     HttpServletResponse response) throws IOException {
    if (!"csv".equals(type)) {
      String msg = "Export type: " + type + " not allowed";
      LOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
      return;
    }

    Order order = orderRepository.findOne(orderId);

    if (order == null) {
      String msg = "Order does not exist.";
      LOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
      return;
    }

    permissionService.canViewOrder(order);

    OrderFileTemplate orderFileTemplate = orderFileTemplateService.getOrderFileTemplate();

    if (orderFileTemplate == null) {
      String msg = "Could not export Order, because Order Template File not found";
      LOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
      return;
    }

    response.setContentType("text/csv");
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
        DISPOSITION_BASE + orderFileTemplate.getFilePrefix() + order.getOrderCode() + ".csv");

    try {
      csvHelper.writeCsvFile(order, orderFileTemplate, response.getWriter());
    } catch (IOException ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error occurred while exporting order to csv.");
      LOGGER.error("Error occurred while exporting order to csv", ex);
    }
  }

  /**
   * Manually retry for transferring order file via FTP after updating or checking the FTP
   * transfer properties.
   *
   * @param id UUID of order
   */
  @RequestMapping(value = "/orders/{id}/retry", method = RequestMethod.GET)
  @ResponseBody
  public ResultDto<Boolean> retryOrderTransfer(@PathVariable("id") UUID id) {
    Order order = orderRepository.findOne(id);

    if (null == order) {
      throw new OrderNotFoundException(id);
    }

    permissionService.canTransferOrder(order);

    if (TRANSFER_FAILED != order.getStatus()) {
      throw new ValidationException(ERROR_ORDER_RETRY_INVALID_STATUS, TRANSFER_FAILED.toString());
    }

    orderService.save(order);
    return new ResultDto<>(TRANSFER_FAILED != order.getStatus());
  }

  /**
   * Gets proof of deliveries related with the given order.
   *
   * @param id UUID of order
   */
  @RequestMapping(value = "/orders/{id}/proofOfDeliveries", method = RequestMethod.GET)
  @ResponseBody
  public ProofOfDeliveryDto getProofOfDeliveries(@PathVariable("id") UUID id) {
    Order order = orderRepository.findOne(id);

    if (null == order) {
      throw new OrderNotFoundException(id);
    }

    permissionService.canViewOrder(order);

    return ProofOfDeliveryDto.newInstance(
        proofOfDeliveryRepository.findByOrderId(id),
        exporter
    );
  }

  private Order createSingleOrder(OrderDto orderDto,
                                  OAuth2Authentication authentication) {
    UserDto currentUser = authenticationHelper.getCurrentUser();
    UUID userId = currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();

    if (!authentication.isClientOnly()) {
      LOGGER.debug("Checking rights to create order");
      permissionService.canEditOrder(orderDto);
    }

    LOGGER.debug("Creating new order");
    return orderService.createOrder(orderDto, userId);
  }
}
