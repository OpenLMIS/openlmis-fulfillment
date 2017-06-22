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
  private OrderSecurityService requisitionSecurityService = new OrderSecurityService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldUseCachedRightIfOneExists() {
    final Order requisition = mockRequisition(UUID.randomUUID());

    List<Order> allRequisitions = Lists.newArrayList(requisition, requisition, requisition);

    requisitionSecurityService.filterInaccessibleOrders(allRequisitions);

    // The permission service should be called only one time, despite having 3 requisitions, due
    // to caching
    verify(permissionService, times(1)).canViewOrderOrManagePod(any(Order.class));
  }

  @Test
  public void shouldNotUseCachedRightIfAllCallsAreDifferent() {
    final Order requisition = mockRequisition(UUID.randomUUID());
    final Order requisition2 = mockRequisition(UUID.randomUUID());
    final Order requisition3 = mockRequisition(UUID.randomUUID());

    List<Order> allRequisitions = Lists.newArrayList(requisition, requisition2, requisition3);

    requisitionSecurityService.filterInaccessibleOrders(allRequisitions);

    // The permission service should be called one time for each requisition (no cache hits)
    verify(permissionService, times(3)).canViewOrderOrManagePod(any(Order.class));
  }

  @Test
  public void shouldProperlyFilterAccessibleRequisitions() {
    final Order requisition = mockRequisition(UUID.randomUUID());
    final Order requisition2 = mockRequisition(UUID.randomUUID());
    final Order requisition3 = mockRequisition(UUID.randomUUID());
    final Order requisition4 = mockRequisition(UUID.randomUUID());

    when(permissionService.canViewOrderOrManagePod(any(Order.class)))
        .thenReturn(true, false, false, true);

    List<Order> allRequisitions = Lists.newArrayList(requisition, requisition2,
        requisition3, requisition4);
    List<Order> result = requisitionSecurityService
        .filterInaccessibleOrders(allRequisitions);

    assertEquals(2, result.size());
    assertEquals(requisition, result.get(0));
    assertEquals(requisition4, result.get(1));
  }

  private Order mockRequisition(UUID facilityId) {
    Order requisition = new Order();
    requisition.setSupplyingFacilityId(facilityId);
    return requisition;
  }
}
