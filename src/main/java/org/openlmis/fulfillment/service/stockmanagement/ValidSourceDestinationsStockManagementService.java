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

package org.openlmis.fulfillment.service.stockmanagement;

import com.google.common.collect.Maps;

import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class ValidSourceDestinationsStockManagementService
    extends BaseStockManagementService<ValidSourceDestinationDto> {

  @Override
  protected Class<ValidSourceDestinationDto> getResultClass() {
    return ValidSourceDestinationDto.class;
  }

  @Override
  protected Class<ValidSourceDestinationDto[]> getArrayResultClass() {
    return ValidSourceDestinationDto[].class;
  }

  /**
   * Try to find an instance of {@link ValidSourceDestinationDto} based on passed parameters.
   */
  public Optional<ValidSourceDestinationDto> findOne(UUID program, UUID facilityType,
                                                     UUID facility) {
    Collection<ValidSourceDestinationDto> sources = search(program, facilityType);

    return sources
        .stream()
        .filter(elem -> elem.getNode().isRefDataFacility())
        .filter(elem -> Objects.equals(facility, elem.getNode().getReferenceId()))
        .findFirst();
  }

  private Collection<ValidSourceDestinationDto> search(UUID program, UUID facilityType) {
    Map<String, Object> map = Maps.newHashMap();
    map.put("program", program);
    map.put("facilityType", facilityType);

    return findAll("", map);
  }
}
