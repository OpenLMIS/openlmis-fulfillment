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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourcesStockManagementService;
import org.openlmis.fulfillment.web.BaseWebIntegrationTest;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;

public class ValidSourcesStockManagementServiceIntegrationTest extends BaseWebIntegrationTest {

  @Inject
  private ValidSourcesStockManagementService validSourcesStockManagementService;

  private final UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");

  private final UUID fromFacilityId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

  private final UUID toFacilityId = UUID.fromString("e6799d64-d10d-4011-b8c2-0e4d4a3f65ce");

  private final UUID nonMatchingId = UUID.fromString("f91de7ba-f63a-49ae-b5a9-9d7746ef250e");

  private static final String VALID_SOURCES_RESPONSE = "{\n"
      + "    {\n"
      + "  \"content\": [\n"
      + "      \"id\": \"5c9ec6f7-2433-4f90-beb0-958e2e6b5835\",\n"
      + "      \"programId\": \"dce17f2e-af3e-40ad-8e00-3496adef44c3\",\n"
      + "      \"facilityTypeId\": \"ac1d268b-ce10-455f-bf87-9c667da8f060\",\n"
      + "      \"node\": {\n"
      + "        \"id\": \"0bd28568-43f1-4836-934d-ec5fb11398e8\",\n"
      + "        \"referenceId\": \"e6799d64-d10d-4011-b8c2-0e4d4a3f65ce\",\n"
      + "        \"refDataFacility\": true\n"
      + "      },\n"
      + "      \"name\": \"Comfort Health Clinic\",\n"
      + "      \"isFreeTextAllowed\": false\n"
      + "    },\n"
      + "    {\n"
      + "      \"id\": \"44c2b8d3-1269-46c5-bcd4-265732e380ea\",\n"
      + "      \"programId\": \"dce17f2e-af3e-40ad-8e00-3496adef44c3\",\n"
      + "      \"facilityTypeId\": \"ac1d268b-ce10-455f-bf87-9c667da8f060\",\n"
      + "      \"node\": {\n"
      + "        \"id\": \"087e81f6-a74d-4bba-9d01-16e0d64e9609\",\n"
      + "        \"referenceId\": \"3b2886f1-632b-4bd5-acd9-aaed37045682\",\n"
      + "        \"refDataFacility\": false\n"
      + "      },\n"
      + "      \"name\": \"NGO\",\n"
      + "      \"isFreeTextAllowed\": true\n"
      + "    },\n"
      + "    {\n"
      + "      \"id\": \"a66eeaa5-efe5-47af-87a8-9e09b18b7aea\",\n"
      + "      \"programId\": \"dce17f2e-af3e-40ad-8e00-3496adef44c3\",\n"
      + "      \"facilityTypeId\": \"e2faaa9e-4b2d-4212-bb60-fd62970b2113\",\n"
      + "      \"node\": {\n"
      + "        \"id\": \"835d3497-b738-4438-8e94-ece41704a86e\",\n"
      + "        \"referenceId\": \"9318b2ea-9ae0-42b2-a7e1-353683f54000\",\n"
      + "        \"refDataFacility\": true\n"
      + "      },\n"
      + "      \"name\": \"Balaka District Warehouse\",\n"
      + "      \"isFreeTextAllowed\": false\n"
      + "    }\n"
      + "  ],\n"
      + "  \"pageable\": {\n"
      + "    \"sort\": [\n"
      + "    ],\n"
      + "    \"pageNumber\": 0,\n"
      + "    \"pageSize\": 2147483647,\n"
      + "    \"offset\": 0,\n"
      + "    \"unpaged\": false,\n"
      + "    \"paged\": true\n"
      + "  },\n"
      + "  \"totalElements\": 3,\n"
      + "  \"last\": false,\n"
      + "  \"totalPages\": 1,\n"
      + "  \"first\": true,\n"
      + "  \"sort\": [\n"
      + "  ],\n"
      + "  \"numberOfElements\": 3,\n"
      + "  \"size\": 2147483647,\n"
      + "  \"number\": 0,\n"
      + "  \"empty\": false\n"
      + "}";

  @Before
  public void setUp() {
    wireMockRule.stubFor(
        get(urlPathEqualTo("/api/validSources"))
            .withQueryParam("facilityId", equalTo(fromFacilityId.toString()))
            .withQueryParam("programId", equalTo(programId.toString()))
            .withQueryParam("page", equalTo(String.valueOf(0)))
            .withQueryParam("size", equalTo(String.valueOf(Integer.MAX_VALUE)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(VALID_SOURCES_RESPONSE))
    );

    wireMockRule.stubFor(
        get(urlPathEqualTo("/api/validSources"))
            .withQueryParam("facilityId", equalTo(nonMatchingId.toString()))
            .withQueryParam("programId", equalTo(nonMatchingId.toString()))
            .withQueryParam("page", equalTo(String.valueOf(0)))
            .withQueryParam("size", equalTo(String.valueOf(Integer.MAX_VALUE)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(MOCK_EMPTY_PAGE))
    );
  }

  public void shouldFindValidSource() {
    UUID expectedValidSourceId = UUID.fromString("5c9ec6f7-2433-4f90-beb0-958e2e6b5835");

    Optional<ValidSourceDestinationDto> searchResult = validSourcesStockManagementService
        .search(programId, fromFacilityId, toFacilityId);

    assertTrue(searchResult.isPresent());
    assertEquals(expectedValidSourceId, searchResult.get().getId());
  }

  @Test
  public void shouldNotFindValidSource() {
    Optional<ValidSourceDestinationDto> searchResult = validSourcesStockManagementService
        .search(nonMatchingId, nonMatchingId, nonMatchingId);

    assertFalse(searchResult.isPresent());
  }
}
