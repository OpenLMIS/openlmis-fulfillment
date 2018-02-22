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

package org.openlmis.fulfillment.testutils;

import org.openlmis.fulfillment.service.referencedata.DispensableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.ProgramOrderableDto;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OrderableDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String productCode;
  private String fullProductName;
  private long netContent;
  private long packRoundingThreshold;
  private boolean roundToZero;
  private Set<ProgramOrderableDto> programs;
  private DispensableDto dispensable;
  private Map<String, String> extraData;

  /**
   * Builder for {@link OrderableDto}.
   */
  public OrderableDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    productCode = "P" + instanceNumber;
    fullProductName = "product" + instanceNumber;
    netContent = 10;
    packRoundingThreshold = 0;
    roundToZero = true;
    programs = new HashSet<>();
    dispensable = new DispensableDto();
    extraData = null;
  }

  /**
   * Builds instance of {@link OrderableDto}.
   */
  public OrderableDto build() {
    OrderableDto orderable = new OrderableDto(productCode, fullProductName, netContent,
        packRoundingThreshold, roundToZero, programs, dispensable, extraData);
    orderable.setId(id);
    return orderable;
  }

  public OrderableDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public OrderableDataBuilder withNetContent(long netContent) {
    this.netContent = netContent;
    return this;
  }
}
