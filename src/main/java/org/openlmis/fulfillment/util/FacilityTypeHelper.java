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

package org.openlmis.fulfillment.util;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_FACILITY_CANNOT_BE_WARD_SERVICE_TYPE;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.FacilityTypeDto;
import org.openlmis.fulfillment.web.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FacilityTypeHelper {

  public static final String WARD_SERVICE_TYPE_CODE = "WS";

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  /**
   * Method check if facility has type ward/service.
   *
   * @param facility Facility.
   * @param facilityFunction the function of the facility being checked (e.g. supplying facility,
   *                         requesting facility)
   */
  public void checkIfFacilityHasSupportedType(FacilityDto facility, String facilityFunction) {
    FacilityTypeDto type = facility.getType();
    if (type != null && type.getCode().equals(WARD_SERVICE_TYPE_CODE)) {
      throw new ValidationException(
          ERROR_FACILITY_CANNOT_BE_WARD_SERVICE_TYPE, facilityFunction
      );
    }
  }

  /**
   * Method check if facilities has type ward/service.
   *
   * @param facilitiesFunctionsMap Map of UUIDs of facilities and their functions (e.g.
   *                               supplying facility, requesting facility)
   */
  public void checkIfFacilityHasSupportedType(Map<UUID, String> facilitiesFunctionsMap) {
    Collection<FacilityDto> facilities = facilityReferenceDataService
        .findByIds(facilitiesFunctionsMap.keySet());
    facilities.forEach(facility -> checkIfFacilityHasSupportedType(facility, facilitiesFunctionsMap
        .get(facility.getId())));
  }



}