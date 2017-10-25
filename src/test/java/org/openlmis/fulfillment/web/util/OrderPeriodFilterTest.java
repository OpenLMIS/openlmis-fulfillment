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

package org.openlmis.fulfillment.web.util;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrderPeriodFilterTest {
  private static final List<BasicOrderDto> ORDERS;

  static {
    ORDERS = IntStream
        .rangeClosed(1, 12)
        .mapToObj(idx -> {
          LocalDate date = LocalDate.of(2017, idx, idx);
          return createOrder(date.with(firstDayOfMonth()), date.with(lastDayOfMonth()));
        })
        .collect(Collectors.toList());
  }

  private static BasicOrderDto createOrder(LocalDate start, LocalDate end) {
    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setStartDate(start);
    period.setEndDate(end);

    BasicOrderDto order = new BasicOrderDto();
    order.setProcessingPeriod(period);

    return order;
  }

  @Test
  public void shouldPassIfPeriodIsBetween() throws Exception {
    shouldPass(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 12, 31));
  }

  @Test
  public void shouldFailIfPeriodIsNotBetween() throws Exception {
    shouldFail(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 12, 31));
  }

  @Test
  public void shouldPassIfPeriodIsAfterStartDate() throws Exception {
    shouldPass(LocalDate.of(2017, 1, 1), null);
  }

  @Test
  public void shouldFailIfPeriodIsBeforeStartDate() throws Exception {
    shouldFail(LocalDate.of(2018, 1, 1), null);
  }

  @Test
  public void shouldPassIfPeriodIsBeforeEndDate() throws Exception {
    shouldPass(null, LocalDate.of(2017, 12, 31));
  }

  @Test
  public void shouldFailIfPeriodIsAfterEndDate() throws Exception {
    shouldFail(null, LocalDate.of(2016, 12, 31));
  }

  private void shouldPass(LocalDate start, LocalDate end) {
    OrderPeriodFilter filter = new OrderPeriodFilter(start, end);
    ORDERS.forEach(order -> assertTrue(filter.test(order)));
  }

  private void shouldFail(LocalDate start, LocalDate end) {
    OrderPeriodFilter filter = new OrderPeriodFilter(start, end);
    ORDERS.forEach(order -> assertFalse(filter.test(order)));
  }
}