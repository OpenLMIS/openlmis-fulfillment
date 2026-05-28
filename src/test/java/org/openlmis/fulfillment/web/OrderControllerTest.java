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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.OrderSender;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.OrderStorage;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ResultDto;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.UpdateDetailsDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.util.IdsDto;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderDtoBuilder;
import org.openlmis.fulfillment.web.validator.OrderValidator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;

@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

  private static final String SERVICE_URL = "localhost";
  @InjectMocks
  private OrderController orderController;
  @Mock
  private AuthenticationHelper authenticationHelper;
  @Mock
  private OrderService orderService;
  @Mock
  private ExporterBuilder exporterBuilder;
  @Mock
  private FacilityReferenceDataService facilities;
  @Mock
  private ProgramReferenceDataService programs;
  @Mock
  private PeriodReferenceDataService periods;
  @Mock
  private UserReferenceDataService users;
  @Mock
  private ShipmentService shipmentService;
  @Mock
  private OrderDtoBuilder orderDtoBuilder;
  @Mock
  private PermissionService permissionService;
  @Mock
  private OrderValidator orderValidator;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private TransferPropertiesRepository transferPropertiesRepository;

  @Mock
  private OrderStorage orderStorage;

  @Mock
  private OrderSender orderSender;

  private UUID lastUpdaterId = UUID.fromString("35316636-6264-6331-2d34-3933322d3462");
  private OAuth2Authentication authentication = mock(OAuth2Authentication.class);
  private UpdateDetails updateDetails = new UpdateDetailsDataBuilder()
      .withUpdaterId(lastUpdaterId)
      .withUpdatedDate(ZonedDateTime.now())
      .build();
  private Order order = new OrderDataBuilder()
      .withStatus(OrderStatus.ORDERED)
      .withUpdateDetails(updateDetails)
      .build();
  private OrderDto orderDto = new OrderDto();

  @Before
  public void setUp() {
    when(orderService.createOrder(orderDto, lastUpdaterId)).thenReturn(order);
    when(authentication.isClientOnly()).thenReturn(true);
    when(orderDtoBuilder.build(order)).thenReturn(orderDto);

    when(shipmentService.create(any(Shipment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, Shipment.class));

    orderDto.setUpdaterId(lastUpdaterId);

    ReflectionTestUtils.setField(exporterBuilder, "serviceUrl", SERVICE_URL);
    ReflectionTestUtils.setField(exporterBuilder, "facilities", facilities);
    ReflectionTestUtils.setField(exporterBuilder, "programs", programs);
    ReflectionTestUtils.setField(exporterBuilder, "periods", periods);
    ReflectionTestUtils.setField(exporterBuilder, "users", users);
  }

  @Test
  public void shouldGetLastUpdaterFromDtoIfCurrentUserIsNull() {
    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    orderController.createOrder(orderDto, authentication);

    verify(orderService).createOrder(eq(orderDto), eq(lastUpdaterId));
  }

  @Test
  public void shouldGetLastUpdaterFromDtoIfCurrentUserIsNullWhenUpdatingOrder() {
    when(authenticationHelper.getCurrentUser()).thenReturn(null);
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.hasErrors()).thenReturn(false);

    orderController.updateOrder(orderDto.getId(), orderDto, bindingResult);

    verify(orderService).updateOrder(eq(orderDto.getId()), eq(orderDto), eq(lastUpdaterId));
  }

  @Test
  public void shouldGetLastUpdaterFromDtoIfCurrentUserIsNullWhenCreatingRequisitionLessOrder() {
    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    orderController.createRequisitionLessOrder(orderDto);

    verify(orderService).createRequisitionLessOrder(eq(orderDto), eq(lastUpdaterId));
  }

  @Test
  public void shouldCreateShipmentForExternalOrder() {
    order.setStatus(OrderStatus.IN_ROUTE);
    orderController.createOrder(orderDto, authentication);

    ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
    verify(shipmentService).create(shipmentCaptor.capture());

    Shipment shipment = shipmentCaptor.getValue();

    assertThat(shipment.getOrder(), is(order));
    assertThat(shipment.getShippedById(), is(order.getCreatedById()));
    assertThat(shipment.getShippedDate(), is(order.getCreatedDate()));
    assertThat(shipment.getNotes(), is(nullValue()));
    assertThat(shipment.getExtraData(), hasEntry("external", "true"));
  }

  @Test
  public void shouldDeleteMultipleOrders() {
    //given

    Order orderTwo = new OrderDataBuilder().withStatus(OrderStatus.CREATING).build();

    List<UUID> ids = new ArrayList<>();
    ids.add(order.getId());
    ids.add(orderTwo.getId());

    List<Order> orders = new ArrayList();
    orders.add(order);
    orders.add(orderTwo);
    when(orderRepository.findByIdInAndStatus(ids, OrderStatus.CREATING)).thenReturn(orders);

    List<UUID> receivingIds = new ArrayList<>();
    receivingIds.add(order.getReceivingFacilityId());
    receivingIds.add(orderTwo.getReceivingFacilityId());

    IdsDto idsDto = new IdsDto();
    idsDto.setIds(ids);

    //when
    orderController.deleteMultipleOrders(idsDto);

    //then
    verify(orderRepository).findByIdInAndStatus(ids, OrderStatus.CREATING);
    verify(permissionService).canDeleteOrders(receivingIds);
    verify(orderRepository).deleteById(order.getId());
    verify(orderRepository).deleteById(orderTwo.getId());
  }

  @Test
  public void retryShouldSendFileAndReturnTrueOnSuccess() {
    Order failedOrder = new OrderDataBuilder()
        .withStatus(OrderStatus.TRANSFER_FAILED)
        .withUpdateDetails(updateDetails)
        .build();
    when(orderRepository.findById(failedOrder.getId()))
        .thenReturn(Optional.of(failedOrder));
    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(), any()))
        .thenReturn(new FtpTransferProperties());
    when(orderSender.send(failedOrder)).thenReturn(true);

    ResultDto<Boolean> result = orderController.retryOrderTransfer(failedOrder.getId());

    assertTrue(result.getResult());
    assertEquals(OrderStatus.IN_ROUTE, failedOrder.getStatus());
    verify(orderStorage).store(failedOrder);
    verify(orderSender).send(failedOrder);
    verify(orderStorage).delete(failedOrder);
    verify(permissionService).canTransferOrder(failedOrder);
  }

  @Test
  public void retryShouldReturnFalseAndKeepStatusWhenSendFails() {
    Order failedOrder = new OrderDataBuilder()
        .withStatus(OrderStatus.TRANSFER_FAILED)
        .withUpdateDetails(updateDetails)
        .build();
    when(orderRepository.findById(failedOrder.getId()))
        .thenReturn(Optional.of(failedOrder));
    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(), any()))
        .thenReturn(new FtpTransferProperties());
    when(orderSender.send(failedOrder)).thenReturn(false);

    ResultDto<Boolean> result = orderController.retryOrderTransfer(failedOrder.getId());

    assertFalse(result.getResult());
    assertEquals(OrderStatus.TRANSFER_FAILED, failedOrder.getStatus());
    verify(orderStorage).store(failedOrder);
    verify(orderSender).send(failedOrder);
    verify(orderStorage, never()).delete(any(Order.class));
  }

  @Test(expected = ValidationException.class)
  public void retryShouldRejectOrdersNotInTransferFailed() {
    Order inRouteOrder = new OrderDataBuilder()
        .withStatus(OrderStatus.IN_ROUTE)
        .withUpdateDetails(updateDetails)
        .build();
    when(orderRepository.findById(inRouteOrder.getId()))
        .thenReturn(Optional.of(inRouteOrder));

    try {
      orderController.retryOrderTransfer(inRouteOrder.getId());
    } finally {
      verify(orderSender, never()).send(any(Order.class));
    }
  }

  @Test(expected = ValidationException.class)
  public void retryShouldThrowWhenNoFtpTargetConfigured() {
    Order failedOrder = new OrderDataBuilder()
        .withStatus(OrderStatus.TRANSFER_FAILED)
        .withUpdateDetails(updateDetails)
        .build();
    when(orderRepository.findById(failedOrder.getId()))
        .thenReturn(Optional.of(failedOrder));
    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(), any()))
        .thenReturn(null);

    try {
      orderController.retryOrderTransfer(failedOrder.getId());
    } finally {
      verify(orderSender, never()).send(any(Order.class));
    }
  }

  @Test(expected = ValidationException.class)
  public void retryShouldThrowWhenTransferPropertiesAreNotFtp() {
    Order failedOrder = new OrderDataBuilder()
        .withStatus(OrderStatus.TRANSFER_FAILED)
        .withUpdateDetails(updateDetails)
        .build();
    when(orderRepository.findById(failedOrder.getId()))
        .thenReturn(Optional.of(failedOrder));
    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(), any()))
        .thenReturn(new LocalTransferProperties());

    try {
      orderController.retryOrderTransfer(failedOrder.getId());
    } finally {
      verify(orderSender, never()).send(any(Order.class));
    }
  }

  @Test(expected = OrderNotFoundException.class)
  public void retryShouldThrowWhenOrderDoesNotExist() {
    UUID missing = UUID.randomUUID();
    when(orderRepository.findById(missing)).thenReturn(Optional.empty());

    try {
      orderController.retryOrderTransfer(missing);
    } finally {
      verify(orderSender, never()).send(any(Order.class));
    }
  }
}
