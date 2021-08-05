/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2021 VillageReach
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;

public class DefaultOrderCreatePostProcessorTest {

  private static final String stringReturnedTrue = "true";
  private static final String stringReturnedFalse = "false";

  @Mock
  private TransferPropertiesRepository transferPropertiesRepository;

  @Mock
  private FulfillmentNotificationService notificationService;

  @Mock
  private OrderStorage orderStorage;

  @Mock
  private OrderSender orderSender;

  @Mock
  private ConfigurationSettingService configurationSettingService;

  @InjectMocks
  private DefaultOrderCreatePostProcessor defaultOrderCreatePostProcessor;

  private Order order;
  private final FtpTransferProperties ftpTransferProperties = new FtpTransferProperties();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(configurationSettingService
        .getAllowFtpTransferOnRequisitionToOrder())
        .thenReturn(stringReturnedTrue);
    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(),any()))
        .thenReturn(ftpTransferProperties);
    when(configurationSettingService.getAllowSendingEmailOnRequisitionToOrder())
        .thenReturn(stringReturnedTrue);
    when(orderSender.send(any(Order.class))).thenReturn(true);
    order = new Order();
    order.setSupplyingFacilityId(UUID.randomUUID());
  }

  @Test
  public void processShouldStoreSendAndNotifyOrderWhenConfigured() {
    // when
    defaultOrderCreatePostProcessor.process(order);

    // then
    verify(orderStorage).store(order);
    verify(orderSender).send(order);
    verify(orderStorage).delete(order);

    verify(notificationService).sendOrderCreatedNotification(order);
  }

  @Test
  public void processShouldNotStoreSendOrderIfAllowFtpTransferIsFalse() {
    // given
    when(configurationSettingService
        .getAllowFtpTransferOnRequisitionToOrder())
        .thenReturn(stringReturnedFalse);

    // when
    defaultOrderCreatePostProcessor.process(order);

    // then
    verify(orderStorage, never()).store(any(Order.class));
    verify(orderSender, never()).send(any(Order.class));
    verify(orderStorage, never()).delete(any(Order.class));

    verify(notificationService).sendOrderCreatedNotification(order);
  }

  @Test
  public void processShouldNotSendNotificationIfAllowSendingEmailIsFalse() {
    // given
    when(configurationSettingService.getAllowSendingEmailOnRequisitionToOrder())
        .thenReturn(stringReturnedFalse);

    // when
    defaultOrderCreatePostProcessor.process(order);

    // then
    verify(orderStorage).store(order);
    verify(orderSender).send(order);
    verify(orderStorage).delete(order);

    verify(notificationService, never()).sendOrderCreatedNotification(any(Order.class));
  }


  @Test
  public void processShouldSaveOrderAndNotDeleteFileIfFtpSendFailure() {
    when(orderSender.send(any(Order.class))).thenReturn(false);

    // when
    defaultOrderCreatePostProcessor.process(order);

    // then
    assertEquals(OrderStatus.TRANSFER_FAILED, order.getStatus());

    verify(orderStorage).store(order);
    verify(orderSender).send(order);
    verify(orderStorage, never()).delete(order);

    verify(notificationService).sendOrderCreatedNotification(order);
  }
}
