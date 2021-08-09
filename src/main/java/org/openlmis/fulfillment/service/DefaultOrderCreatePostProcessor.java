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

import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;

import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.domain.TransferType;
import org.openlmis.fulfillment.extension.point.OrderCreatePostProcessor;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("DefaultOrderCreatePostProcessor")
public class DefaultOrderCreatePostProcessor implements OrderCreatePostProcessor {

  private static final XLogger XLOGGER = XLoggerFactory
      .getXLogger(DefaultOrderCreatePostProcessor.class);

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Autowired
  private FulfillmentNotificationService fulfillmentNotificationService;

  @Autowired
  private OrderStorage orderStorage;

  @Autowired
  private OrderSender orderSender;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Override
  public void process(Order order) {
    XLOGGER.entry(order);
    Profiler profiler = new Profiler("DEFAULT_ORDER_CREATE_POST_PROCESSOR");
    profiler.setLogger(XLOGGER);

    String allowFtpTransfer = configurationSettingService
        .getAllowFtpTransferOnRequisitionToOrder();
    if (allowFtpTransfer == null || "true".equalsIgnoreCase(allowFtpTransfer)) {
      XLOGGER.debug("FTP transfer allowed");
      TransferProperties properties = transferPropertiesRepository
          .findFirstByFacilityIdAndTransferType(order.getSupplyingFacilityId(),
              TransferType.ORDER);

      if (properties instanceof FtpTransferProperties) {
        XLOGGER.debug("Export file and try to send to FTP server");
        orderStorage.store(order);
        boolean success = orderSender.send(order);

        if (success) {
          orderStorage.delete(order);
        } else {
          order.setStatus(TRANSFER_FAILED);
        }
      }
    }

    // Send an email notification to the user that converted the order
    String allowSendingEmail = configurationSettingService
        .getAllowSendingEmailOnRequisitionToOrder();
    if (allowSendingEmail == null || "true".equalsIgnoreCase(allowSendingEmail)) {
      XLOGGER.debug("Notification enabled, send notification");
      fulfillmentNotificationService.sendOrderCreatedNotification(order);
    }

    profiler.stop().log();
    XLOGGER.exit();
  }
}
