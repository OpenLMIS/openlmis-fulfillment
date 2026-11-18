/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2026 VillageReach
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

package org.openlmis.fulfillment.service.transfer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.ExternalApiException;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.util.LocalizedMessageDto;
import org.openlmis.fulfillment.web.util.TransferSendDto;
import org.openlmis.fulfillment.web.util.VersionIdentityDto;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class TransferSendServiceTest {

  @Mock
  private OrderService orderService;

  @Mock
  private ShipmentService shipmentService;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private DateHelper dateHelper;

  @InjectMocks
  private TransferSendService transferSendService;

  private final UUID userId = UUID.randomUUID();
  private final UUID orderId = UUID.randomUUID();
  private final ZonedDateTime now = ZonedDateTime.now();
  private final VersionIdentityDto orderable = new VersionIdentityDto(UUID.randomUUID(), 2L);
  private final VersionIdentityDto otherOrderable = new VersionIdentityDto(UUID.randomUUID(), 1L);
  private final UUID lotA = UUID.randomUUID();
  private final UUID lotB = UUID.randomUUID();

  private TransferSendDto transfer;
  private Order savedOrder;

  @Before
  public void setUp() {
    transfer = new TransferSendDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
        "notes", Arrays.asList(
            new TransferSendDto.LineItem(orderable, lotA, 3L),
            new TransferSendDto.LineItem(orderable, lotB, 4L),
            new TransferSendDto.LineItem(otherOrderable, null, 5L)), null);

    savedOrder = new Order(orderId);

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(now);
    when(orderService.createTransferPush(any(Order.class))).thenAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.setId(orderId);
      return order;
    });
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(savedOrder));
  }

  @Test
  public void shouldCreateTransferPushOrderForReceivingFacility() {
    transferSendService.send(transfer, userId);

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderService).createTransferPush(captor.capture());
    Order order = captor.getValue();

    assertThat(order.getProgramId(), is(transfer.getProgramId()));
    assertThat(order.getSupplyingFacilityId(), is(transfer.getSupplyingFacilityId()));
    assertThat(order.getReceivingFacilityId(), is(transfer.getReceivingFacilityId()));
    assertThat(order.getRequestingFacilityId(), is(transfer.getReceivingFacilityId()));
    assertThat(order.getFacilityId(), is(transfer.getReceivingFacilityId()));
    assertThat(order.getCreatedById(), is(userId));
    assertThat(order.getEmergency(), is(false));
    assertThat(order.getQuotedCost(), is(BigDecimal.ZERO));
    assertThat(order.getUpdateDetails(), is(new UpdateDetails(userId, now)));
  }

  @Test
  public void shouldSumOrderedQuantitiesOfAllLotsPerOrderable() {
    transferSendService.send(transfer, userId);

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderService).createTransferPush(captor.capture());
    List<OrderLineItem> lines = captor.getValue().getOrderLineItems();

    assertThat(lines, hasSize(2));
    assertThat(lines.get(0).getOrderable(), is(new VersionEntityReference(orderable.getId(), 2L)));
    assertThat(lines.get(0).getOrderedQuantity(), is(7L));
    assertThat(lines.get(1).getOrderable(),
        is(new VersionEntityReference(otherOrderable.getId(), 1L)));
    assertThat(lines.get(1).getOrderedQuantity(), is(5L));
  }

  @Test
  public void shouldShipEveryLineWithItsLot() {
    transferSendService.send(transfer, userId);

    ArgumentCaptor<Shipment> captor = ArgumentCaptor.forClass(Shipment.class);
    verify(shipmentService).ship(captor.capture(), eq(userId));
    Shipment shipment = captor.getValue();

    assertThat(shipment.getOrder().getId(), is(orderId));
    assertThat(shipment.getShippedById(), is(userId));
    assertThat(shipment.getShippedDate(), is(now));
    assertThat(shipment.getNotes(), is("notes"));
    assertThat(shipment.getLineItems().stream().map(ShipmentLineItem::getLotId)
        .collect(Collectors.toList()), contains(lotA, lotB, null));
    assertThat(shipment.getLineItems().stream()
        .map(line -> ReflectionTestUtils.getField(line, "quantityShipped"))
        .collect(Collectors.toList()), contains(3L, 4L, 5L));
  }

  @Test
  public void shouldReturnReloadedOrder() {
    assertThat(transferSendService.send(transfer, userId), is(savedOrder));
  }

  @Test(expected = ExternalApiException.class)
  public void shouldPassOnStockEventFailure() {
    doThrow(new ExternalApiException(null, new LocalizedMessageDto()))
        .when(shipmentService).ship(any(Shipment.class), eq(userId));

    transferSendService.send(transfer, userId);
  }
}
