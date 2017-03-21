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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.GeographicZoneDto;

@Getter
@Setter
public class OrderReportFacilityDto extends FacilityDto implements FacilityDto.Exporter {

  public static final Integer DISTRICT_LEVEL = 3;
  public static final Integer REGION_LEVEL = 2;

  /**
   * Get zone of the facility that has the district level.
   * @return district of the facility.
   */
  @JsonIgnore
  public GeographicZoneDto getThirdLevel() {
    return getZoneByLevelNumber(DISTRICT_LEVEL);
  }

  /**
   * Get zone of the facility that has the region level.
   * @return region of the facility.
   */
  @JsonIgnore
  public GeographicZoneDto getSecondLevel() {
    return getZoneByLevelNumber(REGION_LEVEL);
  }
}
