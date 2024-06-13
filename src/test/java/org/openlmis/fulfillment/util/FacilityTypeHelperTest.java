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

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.util.FacilityTypeHelper.WARD_SERVICE_TYPE_CODE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.javers.common.collections.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.FacilityTypeDto;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;
import org.openlmis.fulfillment.testutils.FacilityTypeDataBuilder;
import org.openlmis.fulfillment.web.ValidationException;

@RunWith(MockitoJUnitRunner.class)
public class FacilityTypeHelperTest {

  public static final String TEST_FACILITY = "Test facility";
  public static final String TEST_CODE_TYPE = "TEST";
  public static final String TEST_FACILITY_FUNCTION = "Test facility Function";

  @Mock
  FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  FacilityTypeHelper facilityTypeHelper;

  private FacilityDto facility;

  @Before
  public void setUp() {
    facility = generateInstance(TEST_CODE_TYPE);
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenFacilityIsWardServiceType() {
    FacilityDto ward = generateInstance(WARD_SERVICE_TYPE_CODE);

    facilityTypeHelper.checkIfFacilityHasSupportedType(ward, TEST_FACILITY);
  }

  @Test
  public void shouldPassWhenFacilityIsOtherTypeThanWardService() {
    facilityTypeHelper.checkIfFacilityHasSupportedType(facility, TEST_FACILITY);
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionWhenOneOfTheFacilitiesIsWardServiceType() {
    FacilityDto ward = generateInstance(WARD_SERVICE_TYPE_CODE);
    List<FacilityDto> facilities = Lists.asList(ward, facility);

    Map<UUID, String> facilitiesMap = new HashMap<>();
    facilitiesMap.put(ward.getId(), TEST_FACILITY_FUNCTION);
    facilitiesMap.put(facility.getId(), TEST_FACILITY_FUNCTION);

    when(facilityReferenceDataService.findByIds(anySet())).thenReturn(facilities);

    facilityTypeHelper.checkIfFacilityHasSupportedType(facilitiesMap);
  }

  @Test
  public void shouldPassWhenNoneOfTheFacilitiesAreWardServiceType() {
    FacilityDto anotherFacility = generateInstance("Code2");
    List<FacilityDto> facilities = Lists.asList(anotherFacility, facility);

    Map<UUID, String> facilitiesMap = new HashMap<>();
    facilitiesMap.put(anotherFacility.getId(), TEST_FACILITY_FUNCTION);
    facilitiesMap.put(facility.getId(), TEST_FACILITY_FUNCTION);

    when(facilityReferenceDataService.findByIds(anySet())).thenReturn(facilities);

    facilityTypeHelper.checkIfFacilityHasSupportedType(facilitiesMap);
  }

  private FacilityDto generateInstance(String typeCode) {
    FacilityTypeDto facilityType = new FacilityTypeDataBuilder()
        .withCode(typeCode)
        .build();

    return new FacilityDataBuilder()
        .withType(facilityType)
        .build();
  }

}