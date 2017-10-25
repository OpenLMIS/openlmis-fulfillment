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

import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_IN_USE;
import static org.openlmis.fulfillment.i18n.MessageKeys.FULFILLMENT_EMAIL_ORDER_CREATION_BODY;
import static org.openlmis.fulfillment.i18n.MessageKeys.FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT;

import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.ValidationException;
import org.openlmis.util.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class OrderService {

  static final String[] DEFAULT_COLUMNS = {"facilityCode", "createdDate", "orderNum",
      "productName", "productCode", "orderedQuantity", "filledQuantity"};

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private OrderStorage orderStorage;

  @Autowired
  private OrderSender orderSender;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  protected MessageService messageService;

  @Value("${email.noreply}")
  private String from;

  /**
   * Finds orders matching all of provided parameters.
   *
   * @param params provided parameters.
   * @return ist of Orders with matched parameters.
   */
  public List<Order> searchOrders(OrderSearchParams params) {
    return orderRepository.searchOrders(
        params.getSupplyingFacility(), params.getRequestingFacility(), params.getProgram(),
        params.getProcessingPeriod(), params.getStatusAsEnum()
    );
  }

  /**
   * Safe delete of an order. If the order is linked to an existing proof of delivery, a
   * {@link ValidationException} signals that it cannot be removed.
   *
   * @param order the order to remove
   */
  public void delete(Order order) {
    if (null != proofOfDeliveryRepository.findByOrderId(order.getId())) {
      throw new ValidationException(ERROR_ORDER_IN_USE, order.getId().toString());
    } else {
      orderRepository.delete(order);
    }
  }

  /**
   * Saves a new instance of order. The method also stores the order in local directory and try
   * to send (if there are FTP transfer properties) to an FTP server. Also, the status field in
   * the order will be updated.
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
    sendNotification(saved, saved.getCreatedById());

    return saved;
  }

  private void sendNotification(Order order, UUID userId) {
    String to = userReferenceDataService.findOne(userId).getEmail();
    String subject = messageService
        .localize(new Message(FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT))
        .getMessage();

    String content = createContent(order);

    notificationService.send(new NotificationRequest(from, to, subject, content));
  }

  private String createContent(Order order) {
    String content = messageService
        .localize(new Message(FULFILLMENT_EMAIL_ORDER_CREATION_BODY))
        .getMessage();

    try {
      List<PropertyDescriptor> descriptors = Arrays
          .stream(getPropertyDescriptors(order.getClass()))
          .filter(d -> null != d.getReadMethod())
          .collect(Collectors.toList());

      for (PropertyDescriptor descriptor : descriptors) {
        String target = "{" + descriptor.getName() + "}";
        String replacement = String.valueOf(descriptor.getReadMethod().invoke(order));

        content = content.replace(target, replacement);
      }
    } catch (IllegalAccessException | InvocationTargetException exp) {
      throw new IllegalStateException("Can't get access to getter method", exp);
    }
    return content;
  }

  private void setOrderStatus(Order order) {
    // Is the order associated with a supply line?
    if (null != order.getSupplyingFacilityId()) {
      // Is the supplying facility have the FTP configuration?
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
    } else {
      // Set order status as TRANSFER_FAILED
      order.setStatus(TRANSFER_FAILED);
    }
  }
}
