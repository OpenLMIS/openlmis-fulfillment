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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.i18n.MessageKeys.FULFILLMENT_EMAIL_ORDER_CREATION_BODY;
import static org.openlmis.fulfillment.i18n.MessageKeys.FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.ExternalStatus;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.StatusChange;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.ValidationException;
import org.openlmis.util.NotificationRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private TransferPropertiesRepository transferPropertiesRepository;

  @Mock
  private NotificationService notificationService;

  @Mock
  private OrderStorage orderStorage;

  @Mock
  private OrderSender orderSender;

  @Mock
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Mock
  private ProgramDto program;

  @Mock
  private MessageService messageService;

  @InjectMocks
  private OrderService orderService;

  @Captor
  private ArgumentCaptor<NotificationRequest> notificationCaptor;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(orderService, "from", "noreply@openlmis.org");
    generateMocks();
  }

  @Test
  public void shouldSaveOrder() throws Exception {
    // given
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration("prefix", true, true, true);
    when(orderNumberConfigurationRepository.findAll())
        .thenReturn(Collections.singletonList(orderNumberConfiguration));

    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(true);
    order.setProgramId(program.getId());
    order.setStatus(OrderStatus.ORDERED);
    order.setQuotedCost(BigDecimal.ZERO);
    order.setSupplyingFacilityId(UUID.randomUUID());

    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setOrderedQuantity(1000L);

    order.setOrderLineItems(Lists.newArrayList(orderLineItem));
    order.setCreatedById(UUID.randomUUID());

    StatusChange statusChange = new StatusChange();
    statusChange.setStatus(ExternalStatus.APPROVED);
    statusChange.setCreatedDate(ZonedDateTime.now());
    statusChange.setAuthorId(UUID.randomUUID());
    order.setStatusChanges(Lists.newArrayList(statusChange));

    // when
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderSender.send(order)).thenReturn(true);
    Order created = orderService.save(order);

    // then
    validateCreatedOrder(created, order);
    assertEquals(OrderStatus.IN_ROUTE, created.getStatus());

    InOrder inOrder = inOrder(orderRepository, orderStorage, orderSender);
    inOrder.verify(orderRepository).save(order);
    inOrder.verify(orderStorage).store(order);
    inOrder.verify(orderSender).send(order);
    inOrder.verify(orderStorage).delete(order);

    verify(notificationService).send(notificationCaptor.capture());

    NotificationRequest notification = notificationCaptor.getValue();
    assertThat(notification, is(notNullValue()));

    assertThat(notification.getFrom(), is("noreply@openlmis.org"));
    assertThat(notification.getTo(), is("user@openlmis.org"));
    assertThat(notification.getSubject(), is("New order"));
    assertThat(notification.getContent(),
        is("Create an order: " + order.getId() + " with status: IN_ROUTE"));
  }

  @Test
  public void shouldSaveOrderAndNotDeleteFileIfFtpSendFailure() throws Exception {
    // given
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration("prefix", true, true, true);
    when(orderNumberConfigurationRepository.findAll())
        .thenReturn(Collections.singletonList(orderNumberConfiguration));

    Order order = new Order();
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(true);
    order.setProgramId(program.getId());
    order.setStatus(OrderStatus.ORDERED);
    order.setQuotedCost(BigDecimal.ZERO);
    order.setSupplyingFacilityId(UUID.randomUUID());

    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setOrderedQuantity(1000L);

    order.setOrderLineItems(Lists.newArrayList(orderLineItem));
    order.setCreatedById(UUID.randomUUID());

    StatusChange statusChange = new StatusChange();
    statusChange.setStatus(ExternalStatus.APPROVED);
    statusChange.setCreatedDate(ZonedDateTime.now());
    statusChange.setAuthorId(UUID.randomUUID());
    order.setStatusChanges(Lists.newArrayList(statusChange));

    // when
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderSender.send(order)).thenReturn(false);
    Order created = orderService.save(order);

    // then
    validateCreatedOrder(created, order);
    assertEquals(OrderStatus.TRANSFER_FAILED, created.getStatus());

    InOrder inOrder = inOrder(orderRepository, orderStorage, orderSender);
    inOrder.verify(orderRepository).save(order);
    inOrder.verify(orderStorage).store(order);
    inOrder.verify(orderSender).send(order);
    inOrder.verify(orderStorage, never()).delete(order);
  }

  @Test
  public void shouldFindOrderIfMatchedSupplyingAndRequestingFacilitiesAndProgram() {
    // given
    Order order = generateOrder();

    when(orderRepository.searchOrders(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        order.getProcessingPeriodId(), EnumSet.of(order.getStatus()))
    ).thenReturn(Collections.singletonList(order));

    // when
    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        order.getProcessingPeriodId(), Sets.newHashSet(order.getStatus().toString())
    );
    List<Order> receivedOrders = orderService.searchOrders(params);

    // then
    assertEquals(1, receivedOrders.size());
    assertEquals(receivedOrders.get(0).getSupplyingFacilityId(), order.getSupplyingFacilityId());
    assertEquals(receivedOrders.get(0).getRequestingFacilityId(), order.getRequestingFacilityId());
    assertEquals(receivedOrders.get(0).getProgramId(), order.getProgramId());

    verify(orderRepository, atLeastOnce())
        .searchOrders(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
  }

  @Test
  public void shouldDeleteOrderIfNotUsed() {
    //given
    Order order = generateOrder();
    when(proofOfDeliveryRepository.findByOrderId(order.getId())).thenReturn(null);

    //when
    orderService.delete(order);

    //then
    verify(orderRepository).delete(order);
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenAttemptingToDeleteOrderInUse() {
    //given
    Order order = generateOrder();
    ProofOfDelivery pod = new ProofOfDelivery();
    when(proofOfDeliveryRepository.findByOrderId(order.getId())).thenReturn(pod);

    //when
    orderService.delete(order);
  }

  private Order generateOrder() {
    int number = new Random().nextInt();
    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setProgramId(program.getId());
    order.setCreatedDate(ZonedDateTime.now().plusDays(number));
    order.setCreatedById(UUID.randomUUID());
    order.setQuotedCost(BigDecimal.valueOf(1));
    order.setOrderCode("OrderCode " + number);
    order.setStatus(OrderStatus.ORDERED);
    order.setOrderLineItems(new ArrayList<>());
    order.getOrderLineItems().add(generateOrderLineItem(order));
    return order;
  }

  private OrderLineItem generateOrderLineItem(Order order) {
    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setId(UUID.randomUUID());
    orderLineItem.setFilledQuantity(1000L);
    orderLineItem.setOrder(order);
    orderLineItem.setOrderedQuantity(1000L);
    return orderLineItem;
  }

  private void validateCreatedOrder(Order actual, Order expected) {
    assertEquals(actual.getExternalId(), expected.getExternalId());
    assertEquals(actual.getReceivingFacilityId(), expected.getReceivingFacilityId());
    assertEquals(actual.getRequestingFacilityId(), expected.getRequestingFacilityId());
    assertEquals(actual.getProgramId(), expected.getProgramId());
    assertEquals(actual.getSupplyingFacilityId(), expected.getSupplyingFacilityId());
    assertEquals(1, actual.getOrderLineItems().size());
    assertEquals(1, expected.getOrderLineItems().size());

    OrderLineItem actualLineItem = actual.getOrderLineItems().iterator().next();
    OrderLineItem expectedLineItem = expected.getOrderLineItems().iterator().next();

    assertEquals(expectedLineItem.getOrderedQuantity(), actualLineItem.getOrderedQuantity());
    assertEquals(expectedLineItem.getOrderableId(), actualLineItem.getOrderableId());

    StatusChange actualStatusChange = actual.getStatusChanges().iterator().next();
    StatusChange expectedStatusChange = expected.getStatusChanges().iterator().next();

    assertEquals(expectedStatusChange.getStatus(), actualStatusChange.getStatus());
    assertEquals(expectedStatusChange.getCreatedDate(), actualStatusChange.getCreatedDate());
    assertEquals(expectedStatusChange.getAuthorId(), actualStatusChange.getAuthorId());
  }

  private void generateMocks() {
    ProgramDto programDto = new ProgramDto();
    programDto.setCode("programCode");
    when(programReferenceDataService.findOne(any())).thenReturn(programDto);

    FacilityDto facilityDto = new FacilityDto();
    facilityDto.setCode("FacilityCode");
    when(facilityReferenceDataService.findOne(any())).thenReturn(facilityDto);

    UserDto userDto = new UserDto();
    userDto.setEmail("user@openlmis.org");
    when(userReferenceDataService.findOne(any())).thenReturn(userDto);

    FtpTransferProperties properties = new FtpTransferProperties();
    when(transferPropertiesRepository.findFirstByFacilityId(any())).thenReturn(properties);

    mockMessages();
  }

  private void mockMessages() {
    Message orderCreationSubject = new Message(FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT);
    Message.LocalizedMessage localizedMessage =
        orderCreationSubject.new LocalizedMessage("New order");
    when(messageService.localize(orderCreationSubject))
        .thenReturn(localizedMessage);
    Message orderCreationBody = new Message(FULFILLMENT_EMAIL_ORDER_CREATION_BODY);
    localizedMessage = orderCreationBody
        .new LocalizedMessage("Create an order: {id} with status: {status}");
    when(messageService.localize(orderCreationBody))
        .thenReturn(localizedMessage);
  }
}
