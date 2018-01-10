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

package org.openlmis.fulfillment.web.shipment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;

import java.time.ZonedDateTime;
import java.util.UUID;

@SuppressWarnings("PMD.UnusedPrivateField")
public class ShipmentControllerTest {

  @Mock
  private PermissionService permissionService;

  @Mock
  private ShipmentRepository shipmentRepository;

  @Mock
  private ShipmentDtoBuilder shipmentDtoBuilder;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private DateHelper dateHelper;

  @Mock
  private UserDto userDto;

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private ShipmentController shipmentController = new ShipmentController();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(ZonedDateTime.now());
    when(userDto.getId()).thenReturn(UUID.randomUUID());
    when(authenticationHelper.getCurrentUser()).thenReturn(userDto);
  }

  @Test
  public void shouldUpdateOrderStatusToShipped() {
    ShipmentDto shipmentDto = new ShipmentDtoDataBuilder().build();
    when(shipmentRepository.save(any(Shipment.class)))
        .thenReturn(Shipment.newInstance(shipmentDto));
    when(orderRepository.findOne(shipmentDto.getOrder().getId()))
        .thenReturn(new OrderDataBuilder().build());

    shipmentController.createShipment(shipmentDto);

    ArgumentCaptor<Order> argument = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(argument.capture());
    assertEquals(OrderStatus.SHIPPED, argument.getValue().getStatus());
  }

}
