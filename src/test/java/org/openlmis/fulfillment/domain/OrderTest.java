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

package org.openlmis.fulfillment.domain;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;

public class OrderTest {

  @Test
  public void shouldCheckIfStatusIsOrdered() {
    Order order = new OrderDataBuilder().withOrderedStatus().build();
    assertTrue(order.isOrdered());

    order = new OrderDataBuilder().withFulfillingStatus().build();
    assertFalse(order.isOrdered());
  }

  @Test
  public void shouldPrepareOrderForLocalFulfill() {
    Order order = new OrderDataBuilder().build();
    order.prepareToLocalFulfill();
    assertEquals(OrderStatus.ORDERED, order.getStatus());
  }

  @Test
  public void shouldCheckIfOrderCanBeShipped() {
    Order order = new OrderDataBuilder().withOrderedStatus().build();
    assertTrue(order.canBeShipped());

    order = new OrderDataBuilder().withFulfillingStatus().build();
    assertTrue(order.canBeShipped());

    order.setStatus(OrderStatus.IN_ROUTE);
    assertFalse(order.canBeShipped());

    order.setStatus(OrderStatus.TRANSFER_FAILED);
    assertFalse(order.canBeShipped());

    order.setStatus(OrderStatus.READY_TO_PACK);
    assertFalse(order.canBeShipped());

    order.setStatus(OrderStatus.RECEIVED);
    assertFalse(order.canBeShipped());

    order.setStatus(OrderStatus.SHIPPED);
    assertFalse(order.canBeShipped());
  }
}
