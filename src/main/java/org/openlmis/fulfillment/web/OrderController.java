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
import static org.openlmis.fulfillment.i18n.MessageKeys.ORDER_EXISTS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ORDER_RETRY_INVALID_STATUS;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.openlmis.fulfillment.domain.CreationDetails;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.FileTemplateService;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.OrderCsvHelper;
import org.openlmis.fulfillment.service.OrderSearchParams;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.util.BasicOrderDto;
import org.openlmis.fulfillment.web.util.BasicOrderDtoBuilder;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderDtoBuilder;
import org.openlmis.fulfillment.web.util.OrderReportDto;
import org.openlmis.fulfillment.web.validator.OrderValidator;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@Transactional
@SuppressWarnings("PMD.TooManyMethods")
public class OrderController extends BaseController {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderController.class);
  private static final String DISPOSITION_BASE = "attachment; filename=";
  private static final String TYPE_CSV = "csv";

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private OrderService orderService;

  @Autowired
  private OrderCsvHelper csvHelper;

  @Autowired
  private FileTemplateService fileTemplateService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ShipmentService shipmentService;

  @Autowired
  private OrderDtoBuilder orderDtoBuilder;

  @Autowired
  private BasicOrderDtoBuilder basicOrderDtoBuilder;

  @Autowired
  private OrderValidator orderValidator;

  @Autowired
  private ExporterBuilder exporter;

  @Value("${groupingSeparator}")
  private String groupingSeparator;

  @Value("${groupingSize}")
  private String groupingSize;

  @Value("${dateTimeFormat}")
  private String dateTimeFormat;

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
  public OrderDto createOrder(@RequestBody OrderDto orderDto, OAuth2Authentication authentication) {
    if (orderDto.getExternalId() != null) {
      Order existingOrder = orderRepository.findByExternalId(orderDto.getExternalId());
      if (existingOrder != null) {
        throw new ValidationException(ORDER_EXISTS);
      }
    }

    Order order = createSingleOrder(orderDto, authentication);
    return orderDtoBuilder.build(order);
  }

  /**
   * Allows creating requisition-less orders.
   * If the id is specified, it will be ignored.
   *
   * @param orderDto A order bound to the request body
   * @return created order
   */
  @RequestMapping(value = "/orders/requisitionLess", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public OrderDto createRequisitionLessOrder(@RequestBody OrderDto orderDto) {
    permissionService.canCreateOrder(orderDto);

    orderDto.setId(null);
    orderDto.setExternalId(null);
    orderDto.setQuotedCost(BigDecimal.ZERO);
    orderDto.setEmergency(Boolean.FALSE);

    UserDto currentUser = authenticationHelper.getCurrentUser();
    UUID userId = currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();

    Order order = orderService.createRequisitionLessOrder(orderDto, userId);

    return orderDtoBuilder.build(order);
  }

  /**
   * Allows updating orders.
   *
   * @param orderId UUID of order which we want to update
   * @param orderDto An order bound to the request body
   * @return updated order
   */
  @PutMapping("/orders/{id}")
  @ResponseBody
  public OrderDto updateOrder(
          @PathVariable("id") UUID orderId,
          @RequestBody OrderDto orderDto,
          BindingResult bindingResult
  ) {
    permissionService.canCreateOrder(orderDto);

    orderValidator.validate(orderDto, bindingResult);
    throwValidationExceptionIfHasError(bindingResult);

    UserDto currentUser = authenticationHelper.getCurrentUser();
    UUID userId = currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();

    Order order = orderService.updateOrder(orderId, orderDto, userId);

    return orderDtoBuilder.build(order);
  }

  /**
   * Send requisition-less order.
   *
   * @param orderId UUID of order
   * @param orderDto An order bound to the request body
   */
  @PutMapping("/orders/{id}/requisitionLess/send")
  @ResponseBody
  public void sendRequisitionLessOrder(
          @PathVariable("id") UUID orderId,
          @RequestBody OrderDto orderDto,
          BindingResult bindingResult
  ) {
    permissionService.canCreateOrder(orderDto);

    orderValidator.validate(orderDto, bindingResult);
    throwValidationExceptionIfHasError(bindingResult);

    UserDto currentUser = authenticationHelper.getCurrentUser();
    UUID userId = currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();

    Order order = orderService.updateOrder(orderId, orderDto, userId);
    order.prepareToLocalFulfill();

    orderRepository.save(order);
  }

  /**
   * Allows creating multiple new orders at once in a single transaction.
   * If the id is specified for any of the orders, it will be ignored.
   *
   * @param orders A list of orders to be created
   * @return a list of newly created or existing orders for provided externalIds
   */
  @RequestMapping(value = "/orders/batch", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<BasicOrderDto> batchCreateOrders(@RequestBody List<OrderDto> orders,
      OAuth2Authentication authentication) {
    List<Order> newOrders = orders
        .stream()
        .map(order -> createSingleOrder(order, authentication))
        .collect(Collectors.toList());
    return basicOrderDtoBuilder.build(newOrders);
  }

  /**
   * Search through orders with given parameters.
   *
   * @param params   order search params
   * @param pageable pagination parameters
   * @return OrderDtos.
   */
  @GetMapping("/orders")
  @ResponseBody
  public Page<BasicOrderDto> searchOrders(OrderSearchParams params, Pageable pageable) {
    Profiler profiler = new Profiler("SEARCH_ORDERS");
    profiler.setLogger(XLOGGER);

    profiler.start("SEARCH_ORDERS_IN_SERVICE");
    Page<Order> orders = orderService.searchOrders(params, pageable);

    profiler.start("TO_DTO");
    List<BasicOrderDto> dtos = basicOrderDtoBuilder.build(orders.getContent());
    Page<BasicOrderDto> dtoPage = new PageImpl<>(
        dtos,
        pageable, orders.getTotalElements());

    profiler.stop().log();
    return dtoPage;
  }

  /**
   * Get information about number of orders.
   *
   * @return Data regarding number of orders to be executed and received.
   */
  @GetMapping("/orders/numberOfOrdersData")
  @ResponseBody
  public NumberOfOrdersData getOrdersData() {
    Profiler profiler = new Profiler("COUNT_ORDERS");
    profiler.setLogger(XLOGGER);

    profiler.start("COUNT_ORDERS_IN_SERVICE");
    NumberOfOrdersData ordersData = orderService.getOrdersData();

    profiler.stop().log();
    return ordersData;
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
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    permissionService.canViewOrder(order);
    OrderDto orderDto = orderDtoBuilder.build(order);
    expandDto(orderDto, expand);
    return orderDto;
  }

  /**
   * Retrieves the distinct UUIDs of the available requesting facilities.
   */
  @RequestMapping(value = "/orders/requestingFacilities", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UUID> getRequestingFacilities(
      @RequestParam(name = "supplyingFacilityId", required = false)
          List<UUID> supplyingFacilityIds) {
    return orderRepository.getRequestingFacilities(supplyingFacilityIds);
  }

  /**
   * Returns csv or pdf of defined object in response.
   *
   * @param orderId UUID of order to print
   * @param format  String describing return format (pdf or csv)
   */
  @RequestMapping(value = "/orders/{id}/print", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<byte[]> printOrder(@PathVariable("id") UUID orderId,
                                 @RequestParam("format") String format) throws IOException {

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    permissionService.canViewOrder(order);

    String filePath = "jasperTemplates/ordersJasperTemplate.jrxml";
    ClassLoader classLoader = getClass().getClassLoader();

    Template template = new Template();
    template.setName("ordersJasperTemplate");

    try (InputStream fis = classLoader.getResourceAsStream(filePath)) {
      templateService.createTemplateParameters(template, fis);
    }

    Map<String, Object> params = new HashMap<>();
    params.put("format", format);
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
    decimalFormatSymbols.setGroupingSeparator(groupingSeparator.charAt(0));
    DecimalFormat decimalFormat = new DecimalFormat("", decimalFormatSymbols);
    decimalFormat.setGroupingSize(Integer.parseInt(groupingSize));
    params.put("decimalFormat", decimalFormat);
    params.put("dateTimeFormat", dateTimeFormat);
    OrderReportDto orderDto = OrderReportDto.newInstance(order, exporter);
    params.put("datasource", orderDto.getOrderLineItems());
    params.put("order", orderDto);
    params.put("loggedInUser", null != authenticationHelper
        ? authenticationHelper.getCurrentUser().printName() : null);

    byte[] bytes = jasperReportsViewService.generateReport(template, params);

    MediaType mediaType;
    if (TYPE_CSV.equals(format)) {
      mediaType = new MediaType("text", "csv", StandardCharsets.UTF_8);
    } else if ("xls".equals(format)) {
      mediaType = new MediaType("application", "vnd.ms-excel", StandardCharsets.UTF_8);
    } else if ("html".equals(format)) {
      mediaType = new MediaType("text", "html", StandardCharsets.UTF_8);
    } else {
      mediaType = new MediaType("application", "pdf", StandardCharsets.UTF_8);
    }
    String fileName = template.getName().replaceAll("\\s+", "_");

    return ResponseEntity
        .ok()
        .contentType(mediaType)
        .header("Content-Disposition", "inline; filename=" + fileName + "." + format)
        .body(bytes);
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
      @RequestParam(value = "type", required = false,
          defaultValue = TYPE_CSV) String type,
      HttpServletResponse response) throws IOException {
    if (!TYPE_CSV.equals(type)) {
      String msg = "Export type: " + type + " not allowed";
      XLOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
      return;
    }

    Order order = orderRepository.findById(orderId).orElse(null);

    if (order == null) {
      String msg = "Order does not exist.";
      XLOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
      return;
    }

    permissionService.canViewOrder(order);

    FileTemplate fileTemplate = fileTemplateService.getOrderFileTemplate();

    if (fileTemplate == null) {
      String msg = "Could not export Order, because Order Template File not found";
      XLOGGER.warn(msg);
      response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
      return;
    }

    response.setContentType("text/csv");
    response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
        DISPOSITION_BASE + fileTemplate.getFilePrefix() + order.getOrderCode() + ".csv");

    try {
      csvHelper.writeCsvFile(order, fileTemplate, response.getWriter());
    } catch (IOException ex) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Error occurred while exporting order to csv.");
      XLOGGER.error("Error occurred while exporting order to csv", ex);
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
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    permissionService.canTransferOrder(order);

    if (TRANSFER_FAILED != order.getStatus()) {
      throw new ValidationException(ORDER_RETRY_INVALID_STATUS, TRANSFER_FAILED.toString());
    }

    orderService.save(order);
    return new ResultDto<>(TRANSFER_FAILED != order.getStatus());
  }

  private Order createSingleOrder(OrderDto orderDto,
                                  OAuth2Authentication authentication) {

    XLOGGER.entry(orderDto);
    Profiler profiler = new Profiler("CREATE_SINGLE_ORDER");
    profiler.setLogger(XLOGGER);

    profiler.start("CHECK_ORDER_EXISTS");
    if (orderDto.getExternalId() != null) {
      Order existingOrder = orderRepository.findByExternalId(orderDto.getExternalId());
      if (existingOrder != null) {
        stopProfiler(profiler, orderDto);
        return existingOrder;
      }
    }

    orderDto.setId(null);

    profiler.start("CHECK_PERMISSIONS");
    UserDto currentUser = authenticationHelper.getCurrentUser();

    if (!authentication.isClientOnly()) {
      XLOGGER.debug("Checking rights to create order");
      permissionService.canEditOrder(orderDto);
    }

    profiler.start("GET_CURRENT_USER");
    UUID userId = currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();

    XLOGGER.debug("Creating new order");
    profiler.start("CREATE_ORDER");
    Order order = orderService.createOrder(orderDto, userId);

    if (order.isExternal()) {
      profiler.start("GET_SHIPMENT_LINE_ITEMS");
      List<ShipmentLineItem> items = order
          .getOrderLineItems()
          .stream()
          .map(line -> new ShipmentLineItem(line.getOrderable(), line.getOrderedQuantity()))
          .collect(Collectors.toList());

      profiler.start("CREATE_SHIPMENT");
      Shipment shipment = new Shipment(
          order, new CreationDetails(order.getCreatedById(), order.getCreatedDate()),
          null, items, ImmutableMap.of("external", "true"));

      shipmentService.create(shipment);
    }

    stopProfiler(profiler, orderDto);
    return order;
  }

  void stopProfiler(Profiler profiler, Object... exitArgs) {
    profiler.stop().log();
    XLOGGER.exit(exitArgs);
  }
}
