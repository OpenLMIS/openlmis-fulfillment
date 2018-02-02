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
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.StatusChangeDataBuilder;
import org.openlmis.fulfillment.domain.Base36EncodedOrderNumberGenerator;
import org.openlmis.fulfillment.domain.ExternalStatus;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.StatusChange;
import org.openlmis.fulfillment.extension.ExtensionManager;
import org.openlmis.fulfillment.extension.point.OrderNumberGenerator;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ProcessingPeriodDataBuilder;
import org.openlmis.fulfillment.testutils.ProgramDataBuilder;
import org.openlmis.fulfillment.testutils.UserDataBuilder;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.util.OrderDto;
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

  private static final String FROM_EMAIL = "noreply@openlmis.org";
  private static final String SUBJECT = "New order";

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @Mock
  private ProgramReferenceDataService programReferenceDataService;

  @Mock
  private PeriodReferenceDataService periodReferenceDataService;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

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
  private MessageService messageService;

  @Mock
  private DateHelper dateHelper;

  @Mock
  private ExtensionManager extensionManager;

  @InjectMocks
  private ExporterBuilder exporter;

  @InjectMocks
  private OrderService orderService;

  @Captor
  private ArgumentCaptor<NotificationRequest> notificationCaptor;

  @Captor
  private ArgumentCaptor<Order> orderCaptor;

  private ProgramDto program;
  private FacilityDto facility;
  private ProcessingPeriodDto period;
  private OrderableDto orderable;
  private OrderNumberConfiguration orderNumberConfiguration;
  private Order order;
  private UserDto userDto;
  private FtpTransferProperties properties;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(orderService, "from", FROM_EMAIL);
    generateTestData();
    mockResponses();
  }

  @Test
  public void shouldCreateRegularOrder() throws Exception {
    OrderDto dto = OrderDto.newInstance(order, exporter);
    dto.setId(null);

    Order created = orderService.createOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);

    verify(orderRepository).save(orderCaptor.capture());
    verify(orderStorage).store(any(Order.class));
    verify(orderSender).send(any(Order.class));
    verify(orderStorage).delete(any(Order.class));

    assertEquals(OrderStatus.IN_ROUTE, orderCaptor.getValue().getStatus());

    verify(notificationService).send(notificationCaptor.capture());

    NotificationRequest notification = notificationCaptor.getValue();
    assertThat(notification, is(notNullValue()));

    assertThat(notification.getFrom(), is(FROM_EMAIL));
    assertThat(notification.getTo(), is("user@openlmis.org"));
    assertThat(notification.getSubject(), is(SUBJECT));
    assertThat(notification.getContent(),
        is("Create an order: " + order.getId() + " with status: IN_ROUTE"));
  }

  @Test
  public void shouldCreateOrderForFulfill() throws Exception {
    program.setSupportLocallyFulfilled(true);
    facility.setSupportedPrograms(Collections.singletonList(program));

    OrderDto dto = OrderDto.newInstance(order, exporter);
    dto.setId(null);

    order.setStatus(OrderStatus.ORDERED);

    Order created = orderService.createOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);

    verify(orderRepository).save(orderCaptor.capture());
    verify(orderStorage).store(any(Order.class));
    verify(orderSender).send(any(Order.class));
    verify(orderStorage).delete(any(Order.class));

    assertEquals(OrderStatus.ORDERED, orderCaptor.getValue().getStatus());

    verify(notificationService).send(notificationCaptor.capture());

    NotificationRequest notification = notificationCaptor.getValue();
    assertThat(notification, is(notNullValue()));

    assertThat(notification.getFrom(), is(FROM_EMAIL));
    assertThat(notification.getTo(), is("user@openlmis.org"));
    assertThat(notification.getSubject(), is(SUBJECT));
    assertThat(notification.getContent(),
        is("Create an order: " + order.getId() + " with status: ORDERED"));
  }

  @Test
  public void shouldSaveOrder() throws Exception {
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

    assertThat(notification.getFrom(), is(FROM_EMAIL));
    assertThat(notification.getTo(), is("user@openlmis.org"));
    assertThat(notification.getSubject(), is(SUBJECT));
    assertThat(notification.getContent(),
        is("Create an order: " + order.getId() + " with status: IN_ROUTE"));
  }

  @Test
  public void shouldSaveOrderAndNotDeleteFileIfFtpSendFailure() throws Exception {
    StatusChange statusChange = new StatusChange();
    statusChange.setStatus(ExternalStatus.APPROVED);
    statusChange.setCreatedDate(ZonedDateTime.now());
    statusChange.setAuthorId(UUID.randomUUID());
    order.setStatusChanges(Lists.newArrayList(statusChange));

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
        order.getProcessingPeriodId(), Sets.newHashSet(order.getStatus().toString()), null, null
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

  private void generateTestData() {
    program = new ProgramDataBuilder().build();
    facility = new FacilityDataBuilder()
        .withSupportedPrograms(Collections.singletonList(program))
        .build();
    period = new ProcessingPeriodDataBuilder().build();

    orderNumberConfiguration = new OrderNumberConfiguration("prefix", true, true, true);

    userDto = new UserDataBuilder().build();

    orderable = new OrderableDataBuilder().build();
    OrderLineItem orderLineItem = new OrderLineItemDataBuilder()
        .withOrderedQuantity(100L)
        .withOrderableId(orderable.getId())
        .build();
    StatusChange statusChange = new StatusChangeDataBuilder().build();
    order = new OrderDataBuilder()
        .withQuotedCost(BigDecimal.ZERO)
        .withProgramId(program.getId())
        .withCreatedById(userDto.getId())
        .withEmergencyFlag()
        .withStatus(OrderStatus.IN_ROUTE)
        .withStatusChanges(statusChange)
        .withSupplyingFacilityId(facility.getId())
        .withProcessingPeriodId(period.getId())
        .withLineItems(orderLineItem)
        .build();

    userDto = new UserDataBuilder().build();

    properties = new FtpTransferProperties();
  }

  private void mockResponses() {
    when(programReferenceDataService.findOne(program.getId())).thenReturn(program);
    when(facilityReferenceDataService.findOne(facility.getId())).thenReturn(facility);
    when(periodReferenceDataService.findOne(period.getId())).thenReturn(period);
    when(orderableReferenceDataService.findByIds(any()))
        .thenReturn(Collections.singletonList(orderable));

    when(userReferenceDataService.findOne(any())).thenReturn(userDto);

    when(orderNumberConfigurationRepository.findAll())
        .thenReturn(Collections.singletonList(orderNumberConfiguration));

    when(extensionManager.getExtension(OrderNumberGenerator.POINT_ID, OrderNumberGenerator.class))
        .thenReturn(new Base36EncodedOrderNumberGenerator());

    when(transferPropertiesRepository.findFirstByFacilityId(any())).thenReturn(properties);

    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderSender.send(order)).thenReturn(true);

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(ZonedDateTime.now());

    mockMessages();
  }

  private void mockMessages() {
    Message orderCreationSubject = new Message(FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT);
    Message.LocalizedMessage localizedMessage =
        orderCreationSubject.new LocalizedMessage(SUBJECT);
    when(messageService.localize(orderCreationSubject))
        .thenReturn(localizedMessage);
    Message orderCreationBody = new Message(FULFILLMENT_EMAIL_ORDER_CREATION_BODY);
    localizedMessage = orderCreationBody
        .new LocalizedMessage("Create an order: {id} with status: {status}");
    when(messageService.localize(orderCreationBody))
        .thenReturn(localizedMessage);
  }
}
