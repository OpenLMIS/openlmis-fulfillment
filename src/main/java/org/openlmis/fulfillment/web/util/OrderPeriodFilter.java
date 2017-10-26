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

import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

public class OrderPeriodFilter implements Predicate<BasicOrderDto> {
  private LocalDate periodFrom;
  private LocalDate periodTo;

  public OrderPeriodFilter(LocalDate periodFrom, LocalDate periodTo) {
    this.periodFrom = Optional.ofNullable(periodFrom).orElse(LocalDate.MIN);
    this.periodTo = Optional.ofNullable(periodTo).orElse(LocalDate.MAX);
  }

  @Override
  public boolean test(BasicOrderDto order) {
    ProcessingPeriodDto period = order.getProcessingPeriod();
    LocalDate periodStartDate = period.getStartDate();
    LocalDate periodEndDate = period.getEndDate();

    return inBetween(periodStartDate) || inBetween(periodEndDate);
  }

  private boolean inBetween(LocalDate date) {
    return !date.isBefore(periodFrom) && !date.isAfter(periodTo);
  }

}
