package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.domain.OrderStatus.ORDERED;
import static org.openlmis.fulfillment.domain.OrderStatus.SHIPPED;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_RETRY_INVALID_STATUS;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ConfigurationSettingException;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.service.OrderCsvHelper;
import org.openlmis.fulfillment.service.OrderFileException;
import org.openlmis.fulfillment.service.OrderFileTemplateService;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.OrderStorageException;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

@Controller
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
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private ExporterBuilder exporter;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  /**
   * Allows creating new orders.
   * If the id is specified, it will be ignored.
   *
   * @param orderDto A order bound to the request body
   * @return ResponseEntity containing the created order
   */
  @RequestMapping(value = "/orders", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody

  public OrderDto createOrder(@RequestBody OrderDto orderDto, OAuth2Authentication authentication)
      throws ConfigurationSettingException, OrderStorageException, MissingPermissionException {
    Order order = Order.newInstance(orderDto);

    if (!authentication.isClientOnly()) {
      LOGGER.debug("Checking rights to create order");
      permissionService.canConvertToOrder(order);
    }

    LOGGER.debug("Creating new order");

    ProgramDto program = programReferenceDataService.findOne(order.getProgramId());
    OrderNumberConfiguration orderNumberConfiguration =
        orderNumberConfigurationRepository.findAll().iterator().next();

    order.setId(null);
    order.setOrderCode(orderNumberConfiguration.generateOrderNumber(order, program));
    Order newOrder = orderService.save(order);

    ProofOfDelivery proofOfDelivery = new ProofOfDelivery(newOrder);
    proofOfDeliveryRepository.save(proofOfDelivery);

    LOGGER.debug("Created new order with id: {}", order.getId());
    return OrderDto.newInstance(newOrder, exporter);
  }

  /**
   * Get all orders.
   *
   * @return OrderDtos.
   */
  @RequestMapping(value = "/orders", method = RequestMethod.GET)
  @ResponseBody
  public Iterable<OrderDto> getAllOrders() {
    return OrderDto.newInstance(orderRepository.findAll(), exporter);
  }

  /**
   * Allows updating orders.
   *
   * @param orderDto A order bound to the request body
   * @param orderId  UUID of order which we want to update
   * @return ResponseEntity containing the updated order
   */
  @RequestMapping(value = "/orders/{id}", method = RequestMethod.PUT)
  @ResponseBody
  public OrderDto updateOrder(@RequestBody OrderDto orderDto,
                              @PathVariable("id") UUID orderId) {

    Order orderToUpdate = orderRepository.findOne(orderId);
    if (orderToUpdate == null) {
      orderToUpdate = new Order();
      LOGGER.info("Creating new order");
    } else {
      LOGGER.debug("Updating order with id: {}", orderId);
    }

    Order order = Order.newInstance(orderDto);

    orderToUpdate.updateFrom(order);
    orderToUpdate = orderRepository.save(orderToUpdate);

    LOGGER.debug("Saved order with id: {}", orderToUpdate.getId());

    return OrderDto.newInstance(orderToUpdate, exporter);
  }

  /**
   * Get chosen order.
   *
   * @param orderId UUID of order whose we want to get
   * @return OrderDto.
   */
  @RequestMapping(value = "/orders/{id}", method = RequestMethod.GET)
  public ResponseEntity<OrderDto> getOrder(@PathVariable("id") UUID orderId)
      throws MissingPermissionException {
    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      permissionService.canViewOrder(order);
      return new ResponseEntity<>(OrderDto.newInstance(order, exporter), HttpStatus.OK);
    }
  }

  /**
   * Allows deleting order.
   *
   * @param orderId UUID of order which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/orders/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<OrderDto> deleteOrder(@PathVariable("id") UUID orderId) {
    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      orderRepository.delete(order);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Finds Orders matching all of provided parameters.
   *
   * @param supplyingFacility  supplyingFacility of searched Orders.
   * @param requestingFacility requestingFacility of searched Orders.
   * @param program            program of searched Orders.
   * @return ResponseEntity with list of all Orders matching provided parameters and OK httpStatus.
   */
  @RequestMapping(value = "/orders/search", method = RequestMethod.GET)
  @ResponseBody
  public Iterable<OrderDto> searchOrders(
      @RequestParam(value = "supplyingFacility") UUID supplyingFacility,
      @RequestParam(value = "requestingFacility", required = false)
          UUID requestingFacility,
      @RequestParam(value = "program", required = false) UUID program)
      throws MissingPermissionException {

    List<Order> orders = orderService.searchOrders(supplyingFacility, requestingFacility, program);

    for (Order order : orders) {
      permissionService.canViewOrder(order);
    }

    return OrderDto.newInstance(orders, exporter);
  }

  /**
   * Allows finalizing orders.
   *
   * @param orderId The UUID of the order to finalize
   * @return ResponseEntity with the "#200 OK" HTTP response status on success or ResponseEntity
   *         containing the error description and "#400 Bad Request" status
   */
  @RequestMapping(value = "/orders/{id}/finalize", method = RequestMethod.PUT)
  public ResponseEntity<OrderDto> finalizeOrder(@PathVariable("id") UUID orderId)
      throws InvalidOrderStatusException {

    Order order = orderRepository.findOne(orderId);

    if (order == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (order.getStatus() != ORDERED) {
      throw new InvalidOrderStatusException(ERROR_ORDER_INVALID_STATUS, ORDERED);
    }

    LOGGER.debug("Finalizing the order with id: {}", order);
    order.setStatus(SHIPPED);
    orderRepository.save(order);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Returns csv or pdf of defined object in response.
   *
   * @param orderId  UUID of order to print
   * @param format   String describing return format (pdf or csv)
   * @param response HttpServletResponse object
   */
  @RequestMapping(value = "/orders/{id}/print", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  public void printOrder(@PathVariable("id") UUID orderId,
                         @RequestParam("format") String format,
                         HttpServletResponse response)
      throws IOException, OrderFileException, MissingPermissionException {

    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Order does not exist.");
      return;
    }

    permissionService.canViewOrder(order);

    String[] columns = {"productName", "filledQuantity", "orderedQuantity"};
    if ("pdf".equals(format)) {
      response.setContentType("application/pdf");
      response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
          DISPOSITION_BASE + "order-" + order.getOrderCode() + ".pdf");
      orderService.orderToPdf(order, columns, response.getOutputStream());
    } else {
      response.setContentType("text/csv");
      response.addHeader(HttpHeaders.CONTENT_DISPOSITION,
          DISPOSITION_BASE + "order" + order.getOrderCode() + ".csv");
      orderService.orderToCsv(order, columns, response.getWriter());
    }
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
                     HttpServletResponse response) throws IOException, FulfillmentException {
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
  public ResultDto<Boolean> retryOrderTransfer(@PathVariable("id") UUID id)
      throws FulfillmentException {
    Order order = orderRepository.findOne(id);

    if (null == order) {
      throw new OrderNotFoundException(id);
    }

    permissionService.canTransferOrder(order);

    if (TRANSFER_FAILED != order.getStatus()) {
      throw new InvalidOrderStatusException(ERROR_ORDER_RETRY_INVALID_STATUS, TRANSFER_FAILED);
    }

    orderService.save(order);
    return new ResultDto<>(TRANSFER_FAILED != order.getStatus());
  }

}
