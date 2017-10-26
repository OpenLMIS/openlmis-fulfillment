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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OrderPeriodFilterTest {

  /**
   * Test data.
   */
  @Parameterized.Parameters(name = "{index} = {0} - {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        // one month period
        {LocalDate.of(2017, 1, 1), LocalDate.of(2017, 1, 31)},
        // two months period
        {LocalDate.of(2017, 2, 1), LocalDate.of(2017, 3, 31)},
        // three months period
        {LocalDate.of(2017, 4, 1), LocalDate.of(2017, 6, 30)},
        // four months period
        {LocalDate.of(2017, 7, 1), LocalDate.of(2017, 10, 31)},
        // five months period
        {LocalDate.of(2017, 11, 1), LocalDate.of(2018, 3, 31)},
        // six months period
        {LocalDate.of(2018, 4, 1), LocalDate.of(2018, 9, 30)}
    });
  }

  private LocalDate periodStartDate;
  private LocalDate periodEndDate;
  private BasicOrderDto order;

  /**
   * Creates new instance of test.
   */
  public OrderPeriodFilterTest(LocalDate periodStartDate, LocalDate periodEndDate) {
    this.periodStartDate = periodStartDate;
    this.periodEndDate = periodEndDate;

    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setStartDate(periodStartDate);
    period.setEndDate(periodEndDate);

    order = new BasicOrderDto();
    order.setProcessingPeriod(period);
  }

  @Test
  public void shouldPassIfOrderIsInPeriod() throws Exception {
    // same period
    test(periodStartDate, periodEndDate, true);

    // bigger period
    test(periodStartDate.minusDays(5), periodEndDate.plusDays(5), true);

    // move to left
    test(periodStartDate.minusDays(10), periodEndDate.minusDays(5), true);

    // move to right
    test(periodStartDate.plusDays(5), periodEndDate.plusDays(10), true);

    // smaller end
    test(periodStartDate, periodEndDate.minusDays(5), true);

    // bigger start
    test(periodStartDate.plusDays(5), periodEndDate, true);

    // without end
    test(periodStartDate, null, true);

    // smaller start and without end
    test(periodStartDate.minusDays(5), null, true);

    // bigger start and without end
    test(periodStartDate.plusDays(5), null, true);

    // without start
    test(null, periodEndDate, true);

    // bigger end and without start
    test(null, periodEndDate.plusDays(5), true);

    // smaller end and without start
    test(null, periodEndDate.minusDays(5), true);

    // without period
    test(null, null, true);
  }

  @Test
  public void shouldFailIfOrderIsNotInPeriod() throws Exception {
    // smaller period
    test(periodStartDate.plusDays(5), periodEndDate.minusDays(5), false);

    // not in
    test(periodStartDate.minusYears(1), periodEndDate.minusYears(1), false);
  }

  private void test(LocalDate start, LocalDate end, boolean result) {
    String msg = String.format(
        "Period %s - %s should %s be in interval %s - %s",
        periodStartDate, periodEndDate, (result ? "" : "not"), start, end
    );

    OrderPeriodFilter filter = new OrderPeriodFilter(start, end);
    assertThat(msg, filter.test(order), equalTo(result));
  }
}