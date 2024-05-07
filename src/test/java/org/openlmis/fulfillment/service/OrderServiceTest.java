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

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.javers.common.collections.Sets.asSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;
import static org.openlmis.fulfillment.service.PermissionService.PODS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_VIEW;

import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.StatusChangeDataBuilder;
import org.openlmis.fulfillment.domain.Base36EncodedOrderNumberGenerator;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.OrderStatsData;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.StatusChange;
import org.openlmis.fulfillment.domain.TransferType;
import org.openlmis.fulfillment.extension.ExtensionManager;
import org.openlmis.fulfillment.extension.point.ExtensionPointId;
import org.openlmis.fulfillment.extension.point.OrderCreatePostProcessor;
import org.openlmis.fulfillment.extension.point.OrderNumberGenerator;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PermissionStrings;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramOrderableDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.testutils.ProcessingPeriodDataBuilder;
import org.openlmis.fulfillment.testutils.ProgramDataBuilder;
import org.openlmis.fulfillment.testutils.UserDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.NumberOfOrdersData;
import org.openlmis.fulfillment.web.OrderNotFoundException;
import org.openlmis.fulfillment.web.ValidationException;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SuppressWarnings({"PMD.TooManyMethods"})
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

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
  private DateHelper dateHelper;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private PermissionService permissionService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private EntityManager entityManager;

  @Mock
  private DefaultOrderCreatePostProcessor orderCreatePostProcessor;

  @InjectMocks
  private ExporterBuilder exporter;

  @InjectMocks
  private OrderService orderService;

  @Captor
  private ArgumentCaptor<Order> orderCaptor;

  private ProgramDto program;
  private FacilityDto facility;
  private ProcessingPeriodDto period1;
  private ProcessingPeriodDto period2;
  private OrderableDto orderable;
  private OrderNumberConfiguration orderNumberConfiguration;
  private Order order;
  private UserDto userDto;
  private FtpTransferProperties properties;
  private LocalDate startDate;
  private LocalDate endDate;

  @Before
  public void setUp() {
    generateTestData();
    mockResponses();
  }

  @Test
  public void shouldCreateRegularOrder() {
    // given
    order.setId(null);
    OrderDto dto = OrderDto.newInstance(order, exporter);

    // when
    Order created = orderService.createOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);
    verify(entityManager).persist(orderCaptor.capture());
    assertEquals(OrderStatus.IN_ROUTE, orderCaptor.getValue().getStatus());
  }

  @Test
  public void shouldCreateRegularOrderIfFacilityNotSupportProgram() {
    // given
    facility.setSupportedPrograms(emptyList());
    order.setId(null);
    OrderDto dto = OrderDto.newInstance(order, exporter);
    order.setStatus(OrderStatus.ORDERED);

    // when
    Order created = orderService.createOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);
    verify(entityManager).persist(orderCaptor.capture());
    assertEquals(OrderStatus.IN_ROUTE, orderCaptor.getValue().getStatus());
  }

  @Test
  public void shouldCreateOrderForFulfill() {
    // given
    program.setSupportLocallyFulfilled(true);
    order.setId(null);
    OrderDto dto = OrderDto.newInstance(order, exporter);
    order.setStatus(OrderStatus.ORDERED);

    // when
    Order created = orderService.createOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);
    verify(entityManager).persist(orderCaptor.capture());
    assertEquals(OrderStatus.ORDERED, orderCaptor.getValue().getStatus());
  }

  @Test
  public void shouldSaveOrder() {
    // when
    Order created = orderService.save(order);

    // then
    validateCreatedOrder(created, order);
    assertEquals(OrderStatus.IN_ROUTE, created.getStatus());
  }

  @Test
  public void shouldReturnOrdersData() {
    UserDto user = new UserDataBuilder().build();
    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    PermissionStrings.Handler handler = mock(PermissionStrings.Handler.class);
    when(permissionService.getPermissionStrings(user.getId())).thenReturn(handler);
    when(handler.getFacilityIds(ORDERS_EDIT, ORDERS_VIEW, SHIPMENTS_EDIT, SHIPMENTS_VIEW))
        .thenReturn(newHashSet(order.getSupplyingFacilityId()));
    when(handler.getFacilityIds(PODS_MANAGE, PODS_VIEW))
        .thenReturn(newHashSet(order.getRequestingFacilityId()));

    OrderSearchParams toBeExecutedOrdersParams = new OrderSearchParams();
    Set<String> toBeExecutedOrdersStatuses = newHashSet("FULFILLING", "ORDERED");
    toBeExecutedOrdersParams.setStatus(toBeExecutedOrdersStatuses);
    OrderSearchParams toBeReceivedOrdersParams = new OrderSearchParams();
    Set<String> toBeReceivedOrdersStatuses = newHashSet("READY_TO_PACK", "SHIPPED", "IN_ROUTE");
    toBeReceivedOrdersParams.setStatus(toBeReceivedOrdersStatuses);

    when(orderRepository.countOrders(toBeExecutedOrdersParams, null,
        newHashSet(order.getSupplyingFacilityId()),
        newHashSet(order.getRequestingFacilityId())))
        .thenReturn(100L);
    when(orderRepository.countOrders(toBeReceivedOrdersParams, null,
        newHashSet(order.getSupplyingFacilityId()),
        newHashSet(order.getRequestingFacilityId())))
        .thenReturn(200L);

    NumberOfOrdersData result = orderService.getOrdersData();

    assertEquals(Long.valueOf(100L), result.getOrdersToBeExecuted());
    assertEquals(Long.valueOf(200L), result.getOrdersToBeReceived());
  }

  @Test
  public void shouldFindOrderIfMatchedSupplyingAndRequestingFacilitiesAndProgram() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);
    UserDto user = new UserDataBuilder().build();
    PermissionStrings.Handler handler = mock(PermissionStrings.Handler.class);
    when(handler.getFacilityIds(ORDERS_EDIT, ORDERS_VIEW, SHIPMENTS_EDIT, SHIPMENTS_VIEW))
        .thenReturn(newHashSet(order.getSupplyingFacilityId()));
    when(handler.getFacilityIds(PODS_MANAGE, PODS_VIEW))
        .thenReturn(newHashSet(order.getRequestingFacilityId()));

    when(permissionService.getPermissionStrings(user.getId())).thenReturn(handler);

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        order.getProcessingPeriodId(), Sets.newHashSet(order.getStatus().toString()), null, null,
        null
    );
    when(orderRepository.searchOrders(
        params, asSet(order.getProcessingPeriodId()),
        pageable, newHashSet(order.getSupplyingFacilityId()),
        newHashSet(order.getRequestingFacilityId())))
        .thenReturn(new PageImpl<>(Collections.singletonList(order), pageable, 1));

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(1, receivedOrders.getContent().size());
    assertEquals(receivedOrders.getContent().get(0).getSupplyingFacilityId(),
        order.getSupplyingFacilityId());
    assertEquals(receivedOrders.getContent().get(0).getRequestingFacilityId(),
        order.getRequestingFacilityId());
    assertEquals(receivedOrders.getContent().get(0).getProgramId(), order.getProgramId());

    verify(orderRepository, atLeastOnce())
        .searchOrders(anyObject(), anyObject(), anyObject(), anySet(), anySet());
  }

  @Test
  public void shouldNotCheckPermissionWhenCrossServiceRequest() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        order.getProcessingPeriodId(), Sets.newHashSet(order.getStatus().toString()), null, null,
        null);
    when(orderRepository.searchOrders(
        params, asSet(order.getProcessingPeriodId()), pageable))
        .thenReturn(new PageImpl<>(Collections.singletonList(order), pageable, 1));

    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(receivedOrders.getContent().get(0).getSupplyingFacilityId(),
        order.getSupplyingFacilityId());
    assertEquals(receivedOrders.getContent().get(0).getRequestingFacilityId(),
        order.getRequestingFacilityId());
    assertEquals(receivedOrders.getContent().get(0).getProgramId(), order.getProgramId());

    verify(orderRepository, atLeastOnce())
        .searchOrders(anyObject(), anyObject(), anyObject());

    verify(permissionService, never()).getPermissionStrings(anyObject());
  }

  @Test
  public void shouldSearchByStartDateAndEndDate() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        null, Sets.newHashSet(order.getStatus().toString()), startDate, endDate, null);
    when(orderRepository.searchOrders(
        params, asSet(period1.getId(), period2.getId()), pageable))
        .thenReturn(new PageImpl<>(Collections.singletonList(order), pageable, 1));

    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(1, receivedOrders.getContent().size());
    assertEquals(order, receivedOrders.getContent().get(0));

    verify(orderRepository, atLeastOnce()).searchOrders(anyObject(), anyObject(), anyObject());
  }

  @Test
  public void shouldReturnEmptyPageIfFilteredPeriodsAndGivenPeriodIdDoesNotMatch() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);

    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        order.getProcessingPeriodId(), Sets.newHashSet(order.getStatus().toString()),
        startDate, endDate, null);
    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(0, receivedOrders.getContent().size());
    verify(orderRepository, never())
        .searchOrders(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
  }

  @Test
  public void shouldReturnEmptyPageIfPeriodsSearchReturnsEmptyList() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);

    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    when(periodReferenceDataService.search(startDate, endDate)).thenReturn(emptyList());

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        null, Sets.newHashSet(order.getStatus().toString()),
        startDate, endDate, null);
    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(0, receivedOrders.getContent().size());
    verify(orderRepository, never())
        .searchOrders(anyObject(), anyObject(), anyObject(), anyObject(), anyObject());
  }

  @Test
  public void shouldSearchByStartDateAndEndDateAndPeriodId() {
    Order order = generateOrder();
    Pageable pageable = PageRequest.of(0, 10);

    OrderSearchParams params = new OrderSearchParams(
        order.getSupplyingFacilityId(), order.getRequestingFacilityId(), order.getProgramId(),
        period1.getId(), Sets.newHashSet(order.getStatus().toString()), startDate, endDate, null);
    when(orderRepository.searchOrders(
        params, asSet(period1.getId()), pageable))
        .thenReturn(new PageImpl<>(Collections.singletonList(order), pageable, 1));

    when(authenticationHelper.getCurrentUser()).thenReturn(null);

    Page<Order> receivedOrders = orderService.searchOrders(params, pageable);

    assertEquals(1, receivedOrders.getContent().size());
    assertEquals(order, receivedOrders.getContent().get(0));

    verify(orderRepository, atLeastOnce()).searchOrders(anyObject(), anyObject(), anyObject());
  }

  @Test
  public void shouldCreateRequisitionLessOrder() {
    // given
    order.setId(null);
    OrderDto dto = OrderDto.newInstance(order, exporter);

    // when
    Order created = orderService.createRequisitionLessOrder(dto, userDto.getId());

    // then
    validateCreatedOrder(created, order);
    verify(entityManager).persist(orderCaptor.capture());
    assertEquals(OrderStatus.CREATING, orderCaptor.getValue().getStatus());
  }

  @Test
  public void shouldUpdateOrder() {
    // given
    order.setId(UUID.randomUUID());
    order.setStatus(OrderStatus.CREATING);
    OrderDto dto = OrderDto.newInstance(order, exporter);

    // when
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    Order updated = orderService.updateOrder(order.getId(), dto, userDto.getId());

    // then
    validateCreatedOrder(updated, order);
    verify(orderRepository).save(order);
  }

  @Test
  public void shouldThrowExceptionWhenUpdateNonExistingOrder() {
    // given
    order.setId(UUID.randomUUID());
    order.setStatus(OrderStatus.CREATING);
    OrderDto dto = OrderDto.newInstance(order, exporter);

    // when
    when(orderRepository.findById(order.getId())).thenReturn(Optional.empty());

    exception.expect(OrderNotFoundException.class);
    orderService.updateOrder(order.getId(), dto, userDto.getId());
  }

  @Test
  public void shouldThrowExceptionWhenUpdateOrderWithWrongStatus() {
    // given
    order.setId(UUID.randomUUID());
    order.setStatus(OrderStatus.ORDERED);
    OrderDto dto = OrderDto.newInstance(order, exporter);

    // when
    when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

    exception.expect(ValidationException.class);
    orderService.updateOrder(order.getId(), dto, userDto.getId());
  }

  @Test
  public void shouldReturnOrderStatsData() {
    // given
    int numberOfStatuses = OrderStatus.values().length;

    // when
    OrderStatsData result = orderService.getStatusesStatsData(userDto.getHomeFacilityId());

    // then
    verify(orderRepository, times(numberOfStatuses))
        .countByFacilityIdAndStatus(anyObject(), anyObject());
    assertEquals(userDto.getHomeFacilityId(), result.getFacilityId());
    assertEquals(numberOfStatuses, result.getStatusesStats().size());
  }

  private Order generateOrder() {
    return new OrderDataBuilder().withOrderedStatus().build();
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
    assertEquals(expectedLineItem.getOrderable(), actualLineItem.getOrderable());

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
    period1 = new ProcessingPeriodDataBuilder().build();
    period2 = new ProcessingPeriodDataBuilder().build();

    orderNumberConfiguration = new OrderNumberConfiguration("prefix", true, true, true);

    userDto = new UserDataBuilder().build();

    orderable = new OrderableDataBuilder()
        .withPrograms(Collections.singleton(new ProgramOrderableDto()))
        .build();
    OrderLineItem orderLineItem = new OrderLineItemDataBuilder()
        .withOrderedQuantity(100L)
        .withOrderable(orderable.getId(), orderable.getVersionNumber())
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
        .withProcessingPeriodId(period1.getId())
        .withLineItems(orderLineItem)
        .withFacilityId(null)
        .withReceivingFacilityId(null)
        .withRequestingFacilityId(null)
        .build();

    userDto = new UserDataBuilder().build();

    properties = new FtpTransferProperties();
    properties.setTransferType(TransferType.ORDER);

    startDate = LocalDate.now();
    endDate = startDate.plusMonths(1);
  }

  private void mockResponses() {
    when(programReferenceDataService.findOne(program.getId())).thenReturn(program);
    when(facilityReferenceDataService.findOne(facility.getId())).thenReturn(facility);
    when(periodReferenceDataService.findOne(period1.getId())).thenReturn(period1);

    when(userReferenceDataService.findOne(any())).thenReturn(userDto);

    when(orderNumberConfigurationRepository.findAll())
        .thenReturn(Collections.singletonList(orderNumberConfiguration));

    when(extensionManager.getExtension(OrderNumberGenerator.POINT_ID, OrderNumberGenerator.class))
        .thenReturn(new Base36EncodedOrderNumberGenerator());

    when(extensionManager.getExtension(ExtensionPointId.ORDER_CREATE_POST_POINT_ID,
        OrderCreatePostProcessor.class))
        .thenReturn(orderCreatePostProcessor);

    doNothing().when(orderCreatePostProcessor).process(order);

    when(transferPropertiesRepository
        .findFirstByFacilityIdAndTransferType(any(),any()))
        .thenReturn(properties);

    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(ZonedDateTime.now());

    when(periodReferenceDataService.search(startDate, endDate))
        .thenReturn(asList(period1, period2));

    when(orderableReferenceDataService.findByIdentities(anySet())).thenReturn(
        Collections.singletonList(orderable));
  }
}
