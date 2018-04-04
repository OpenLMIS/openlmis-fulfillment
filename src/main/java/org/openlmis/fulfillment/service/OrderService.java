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

package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;
import static org.openlmis.fulfillment.service.PermissionService.PODS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_VIEW;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.extension.ExtensionManager;
import org.openlmis.fulfillment.extension.point.OrderNumberGenerator;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PermissionStrings;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.util.Pagination;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class OrderService {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(OrderService.class);

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Autowired
  private FulfillmentNotificationService fulfillmentNotificationService;

  @Autowired
  private OrderStorage orderStorage;

  @Autowired
  private OrderSender orderSender;

  @Autowired
  private ProgramReferenceDataService programReferenceDataService;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private ExtensionManager extensionManager;

  @Autowired
  private DateHelper dateHelper;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  /**
   * Creates an order.
   *
   * @param orderDto object that order will be created from.
   * @return created Order.
   */
  public Order createOrder(OrderDto orderDto, UUID userId) {
    Order order = Order.newInstance(orderDto,
        new UpdateDetails(userId, dateHelper.getCurrentDateTimeWithSystemZone()));

    ProgramDto program = programReferenceDataService.findOne(order.getProgramId());

    OrderNumberConfiguration orderNumberConfiguration =
        orderNumberConfigurationRepository.findAll().iterator().next();

    OrderNumberGenerator orderNumberGenerator = extensionManager.getExtension(
        OrderNumberGenerator.POINT_ID, OrderNumberGenerator.class);

    String orderNumber = orderNumberGenerator.generate(order);

    order.setOrderCode(orderNumberConfiguration.formatOrderNumber(order, program, orderNumber));
    Order newOrder = save(order);

    XLOGGER.debug("Created new order with id: {}", order.getId());
    return newOrder;
  }

  /**
   * Finds orders matching all of provided parameters.
   *
   * @param params provided parameters.
   * @return ist of Orders with matched parameters.
   */
  public Page<Order> searchOrders(OrderSearchParams params, Pageable pageable) {
    UserDto user = authenticationHelper.getCurrentUser();

    Set<UUID> supplyingFacilities = new HashSet<>();
    Set<UUID> requestingFacilities = new HashSet<>();

    UUID supplyingFacilityId = params.getSupplyingFacilityId();
    UUID requestingFacilityId = params.getRequestingFacilityId();
    if (null != user) {
      PermissionStrings.Handler handler = permissionService.getPermissionStrings(user.getId());

      supplyingFacilities.addAll(
          handler.getFacilityIds(ORDERS_EDIT, ORDERS_VIEW, SHIPMENTS_EDIT, SHIPMENTS_VIEW)
      );

      if (null != supplyingFacilityId) {
        if (!supplyingFacilities.contains(supplyingFacilityId)) {
          return Pagination.getPage(Collections.emptyList(), pageable);
        }
        supplyingFacilities.removeIf(id -> !id.equals(supplyingFacilityId));
      }

      requestingFacilities.addAll(handler.getFacilityIds(PODS_MANAGE, PODS_VIEW));

      if (null != requestingFacilityId) {
        if (!requestingFacilities.contains(requestingFacilityId)) {
          return Pagination.getPage(Collections.emptyList(), pageable);
        }
        requestingFacilities.removeIf(id -> !id.equals(requestingFacilityId));
      }

      if (isEmpty(supplyingFacilities) && isEmpty(requestingFacilities)) {
        // missing rights
        return Pagination.getPage(Collections.emptyList(), pageable);
      }
    } else {
      Optional
          .ofNullable(supplyingFacilityId)
          .ifPresent(supplyingFacilities::add);

      Optional
          .ofNullable(requestingFacilityId)
          .ifPresent(requestingFacilities::add);
    }

    return orderRepository.searchOrders(
        supplyingFacilities, requestingFacilities, params.getProgramId(),
        params.getProcessingPeriodId(), params.getStatusAsEnum(), pageable
    );
  }

  /**
   * Saves a new instance of order. The method also stores the order in local directory and try to
   * send (if there are FTP transfer properties) to an FTP server. Also, the status field in the
   * order will be updated.
   *
   * @param order instance
   * @return passed instance after save.
   */
  public Order save(Order order) {
    setOrderStatus(order);

    // save order
    Order saved = orderRepository.save(order);

    orderStorage.store(saved);

    TransferProperties properties = transferPropertiesRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    if (properties instanceof FtpTransferProperties) {
      boolean success = orderSender.send(saved);

      if (success) {
        orderStorage.delete(saved);
      } else {
        order.setStatus(TRANSFER_FAILED);
        saved = orderRepository.save(order);
      }
    }

    // Send an email notification to the user that converted the order
    fulfillmentNotificationService.sendOrderCreatedNotification(saved);

    return saved;
  }

  private void setOrderStatus(Order order) {
    // Is the order associated with a supply line?
    if (null != order.getSupplyingFacilityId()) {
      // Is the supplying facility have the FTP configuration?

      ProgramDto program = programReferenceDataService.findOne(order.getProgramId());
      Optional<ProgramDto> supportedProgram = facilityReferenceDataService
          .findOne(order.getSupplyingFacilityId())
          .getSupportedPrograms()
          .stream()
          .filter(p -> program.getCode().equals(p.getCode()))
          .findFirst();

      if (supportedProgram.isPresent() && supportedProgram.get().isSupportLocallyFulfilled()) {
        order.prepareToLocalFulfill();
      } else {
        TransferProperties properties = transferPropertiesRepository
            .findFirstByFacilityId(order.getSupplyingFacilityId());

        if (null == properties) {
          // Set order status as TRANSFER_FAILED
          order.setStatus(TRANSFER_FAILED);
        } else {
          // Is the export-orders flag enabled on the supply line associated with the order
          // yes -> Set order status as IN_ROUTE
          // no  -> Set order status as READY_TO_PACK
          order.setStatus(properties instanceof FtpTransferProperties ? IN_ROUTE : READY_TO_PACK);
        }
      }
    } else {
      // Set order status as TRANSFER_FAILED
      order.setStatus(TRANSFER_FAILED);
    }
  }
}
