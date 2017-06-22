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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.fulfillment.domain.Order;

import java.util.List;
import java.util.UUID;

public class OrderSecurityServiceTest {

  @Mock
  private PermissionService permissionService;

  @InjectMocks
  private OrderSecurityService orderSecurityService = new OrderSecurityService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldUseCachedRightIfOneExists() {
    final Order order = mockOrder(UUID.randomUUID());

    List<Order> orders = Lists.newArrayList(order, order, order);

    orderSecurityService.filterInaccessibleOrders(orders);

    // The permission service should be called only one time, despite having 3 orders, due
    // to caching
    verify(permissionService, times(1)).canViewOrderOrManagePod(any(Order.class));
  }

  @Test
  public void shouldNotUseCachedRightIfAllCallsAreDifferent() {
    final Order order = mockOrder(UUID.randomUUID());
    final Order order2 = mockOrder(UUID.randomUUID());
    final Order order3 = mockOrder(UUID.randomUUID());

    List<Order> orders = Lists.newArrayList(order, order2, order3);

    orderSecurityService.filterInaccessibleOrders(orders);

    // The permission service should be called one time for each order (no cache hits)
    verify(permissionService, times(3)).canViewOrderOrManagePod(any(Order.class));
  }

  @Test
  public void shouldProperlyFilterAccessibleOrders() {
    final Order order = mockOrder(UUID.randomUUID());
    final Order order2 = mockOrder(UUID.randomUUID());
    final Order order3 = mockOrder(UUID.randomUUID());
    final Order order4 = mockOrder(UUID.randomUUID());

    when(permissionService.canViewOrderOrManagePod(any(Order.class)))
        .thenReturn(true, false, false, true);

    List<Order> orders = Lists.newArrayList(order, order2, order3, order4);
    List<Order> result = orderSecurityService.filterInaccessibleOrders(orders);

    assertEquals(2, result.size());
    assertEquals(order, result.get(0));
    assertEquals(order4, result.get(1));
  }

  private Order mockOrder(UUID facilityId) {
    Order order = new Order();
    order.setSupplyingFacilityId(facilityId);
    return order;
  }
}
