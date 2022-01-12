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
import static org.openlmis.fulfillment.domain.OrderStatus.FULFILLING;
import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.ORDERED;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.domain.OrderStatus.RECEIVED;
import static org.openlmis.fulfillment.domain.OrderStatus.SHIPPED;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;

import java.util.UUID;

import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;

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
    assertEquals(ORDERED, order.getStatus());
  }

  @Test
  public void shouldCheckIfOrderIsExternal() {
    assertTrue(new OrderDataBuilder().withStatus(TRANSFER_FAILED).build().isExternal());
    assertTrue(new OrderDataBuilder().withStatus(IN_ROUTE).build().isExternal());
    assertTrue(new OrderDataBuilder().withStatus(READY_TO_PACK).build().isExternal());

    assertFalse(new OrderDataBuilder().withStatus(ORDERED).build().isExternal());
    assertFalse(new OrderDataBuilder().withStatus(FULFILLING).build().isExternal());
    assertFalse(new OrderDataBuilder().withStatus(SHIPPED).build().isExternal());
    assertFalse(new OrderDataBuilder().withStatus(RECEIVED).build().isExternal());
  }

  @Test
  public void shouldCheckIfOrderCanBeShipped() {
    Order order = new OrderDataBuilder().withOrderedStatus().build();
    assertTrue(order.canBeFulfilled());

    order = new OrderDataBuilder().withFulfillingStatus().build();
    assertTrue(order.canBeFulfilled());

    order.setStatus(IN_ROUTE);
    assertFalse(order.canBeFulfilled());

    order.setStatus(TRANSFER_FAILED);
    assertFalse(order.canBeFulfilled());

    order.setStatus(READY_TO_PACK);
    assertFalse(order.canBeFulfilled());

    order.setStatus(RECEIVED);
    assertFalse(order.canBeFulfilled());

    order.setStatus(SHIPPED);
    assertFalse(order.canBeFulfilled());
  }

  @Test
  public void shouldUpdateFromSetOrderQuantityOnLineItems() {
    Long expectedOrderQuantity = 1L;
    UUID orderItemId = UUID.randomUUID();

    OrderLineItem newLineItem1 = new OrderLineItemDataBuilder()
            .withOrderedQuantity(expectedOrderQuantity)
            .build();

    newLineItem1.setId(orderItemId);

    OrderLineItem newLineItem2 = new OrderLineItemDataBuilder()
            .withOrderedQuantity(expectedOrderQuantity)
            .build();

    newLineItem2.setId(UUID.randomUUID());


    Order orderWithNewLineItems = new OrderDataBuilder()
            .withLineItems(
                    newLineItem1,
                    newLineItem2
            )
            .build();

    OrderLineItem lineItem = new OrderLineItemDataBuilder()
            .withOrderedQuantity(expectedOrderQuantity + expectedOrderQuantity)
            .build();

    lineItem.setId(orderItemId);

    Order order = new OrderDataBuilder()
            .withLineItems(lineItem)
            .build();

    order.updateFrom(orderWithNewLineItems);

    order.getOrderLineItems().forEach(item ->
            assertEquals(expectedOrderQuantity, item.getOrderedQuantity())
    );
  }
}
