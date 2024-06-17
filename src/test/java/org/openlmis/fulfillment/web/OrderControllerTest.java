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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ShipmentService;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.UpdateDetailsDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.FacilityTypeHelper;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderDtoBuilder;
import org.openlmis.fulfillment.web.validator.OrderValidator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;

@SuppressWarnings("PMD.UnusedPrivateField")
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
  private FacilityTypeHelper facilityTypeHelper;
  @Mock
  private OrderRepository orderRepository;
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
    doNothing().when(facilityTypeHelper).checkIfFacilityHasSupportedType(anyMap());
    orderDto.setReceivingFacility(new FacilityDataBuilder().build());
    orderDto.setRequestingFacility(new FacilityDataBuilder().build());
    orderDto.setSupplyingFacility(new FacilityDataBuilder().build());

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
    orders.add(orderTwo);
    when(orderRepository.findAllByIdAndStatus(ids, OrderStatus.CREATING.name())).thenReturn(orders);

    List<UUID> receivingIds = new ArrayList<>();
    receivingIds.add(orderTwo.getReceivingFacilityId());

    //when
    orderController.deleteMultipleOrders(ids);

    //then
    verify(orderRepository).findAllByIdAndStatus(ids, OrderStatus.CREATING.name());
    verify(permissionService).canDeleteOrders(receivingIds);
    verify(orderRepository).deleteById(orderTwo.getId());
  }
}
