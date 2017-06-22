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

import org.openlmis.fulfillment.domain.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderSecurityService {

  @Autowired
  private PermissionService permissionService;

  /**
   * Filters orders based on user permissions. It strives to make as little calls to the
   * reference data service as possible.
   *
   * @param orders input list containing any orders
   * @return filtered input list of orders, containing only those that the user has access to
   */
  public List<Order> filterInaccessibleOrders(List<Order> orders) {
    Map<UUID, Boolean> verified = new HashMap<>();
    List<Order> filteredList = new ArrayList<>();

    for (Order order : orders) {
      Boolean accessible = verified.computeIfAbsent(
          order.getSupplyingFacilityId(),
          key -> permissionService.canViewOrderOrManagePod(order)
      );

      if (accessible) {
        filteredList.add(order);
      }
    }

    return filteredList;
  }

}
