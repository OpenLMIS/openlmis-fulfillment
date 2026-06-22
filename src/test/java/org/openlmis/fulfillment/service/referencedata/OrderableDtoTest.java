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

package org.openlmis.fulfillment.service.referencedata;

import static org.junit.Assert.assertEquals;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;

public class OrderableDtoTest {

  @Test
  public void equalsContract() throws Exception {
    EqualsVerifier.forClass(OrderableDto.class)
        .withRedefinedSuperclass()
        .suppress(Warning.STRICT_INHERITANCE) // suppress class not final
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void packsToOrderShouldReturnPacksForExactMultiple() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .withPackRoundingThreshold(0)
        .withRoundToZero(false)
        .build();

    assertEquals(2L, orderable.packsToOrder(20));
  }

  @Test
  public void packsToOrderShouldRoundUpWhenRemainderExceedsThreshold() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .withPackRoundingThreshold(0)
        .withRoundToZero(false)
        .build();

    assertEquals(3L, orderable.packsToOrder(23));
  }

  @Test
  public void packsToOrderShouldNotRoundUpWhenRemainderWithinThreshold() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .withPackRoundingThreshold(5)
        .withRoundToZero(false)
        .build();

    assertEquals(2L, orderable.packsToOrder(23));
  }

  @Test
  public void packsToOrderShouldRoundToOneWhenResultZeroAndRoundToZeroFalse() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .withPackRoundingThreshold(5)
        .withRoundToZero(false)
        .build();

    assertEquals(1L, orderable.packsToOrder(3));
  }

  @Test
  public void packsToOrderShouldRoundToZeroWhenResultZeroAndRoundToZeroTrue() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .withPackRoundingThreshold(5)
        .withRoundToZero(true)
        .build();

    assertEquals(0L, orderable.packsToOrder(3));
  }

  @Test
  public void packsToOrderShouldReturnZeroForZeroNetContent() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(0)
        .build();

    assertEquals(0L, orderable.packsToOrder(10));
  }

  @Test
  public void packsToOrderShouldReturnZeroForNonPositiveQuantity() {
    OrderableDto orderable = new OrderableDataBuilder()
        .withNetContent(10)
        .build();

    assertEquals(0L, orderable.packsToOrder(0));
    assertEquals(0L, orderable.packsToOrder(-5));
  }
}
