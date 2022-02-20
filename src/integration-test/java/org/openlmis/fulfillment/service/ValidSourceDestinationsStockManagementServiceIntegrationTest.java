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
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.web.BaseWebIntegrationTest;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;

public class ValidSourceDestinationsStockManagementServiceIntegrationTest
    extends BaseWebIntegrationTest {

  @Inject
  private ValidDestinationsStockManagementService validDestinationsStockManagementService;

  private final UUID programId = UUID.fromString("dce17f2e-af3e-40ad-8e00-3496adef44c3");

  private final UUID fromFacilityId = UUID.fromString("ac1d268b-ce10-455f-bf87-9c667da8f060");

  private final UUID toFacilityId = UUID.fromString("13037147-1769-4735-90a7-b9b310d128b8");

  private final UUID nonMatchingId = UUID.fromString("f91de7ba-f63a-49ae-b5a9-9d7746ef250e");

  private static final String VALID_DESTINATIONS_RESPONSE = "{"
      + "\"content\":["
      + "{"
      + "\"id\":\"8059d138-f34e-4286-bc79-4b8f60aecca6\","
      + "\"programId\":\"10845cb9-d365-4aaa-badd-b4fa39c6a26a\","
      + "\"facilityTypeId\":\"ac1d268b-ce10-455f-bf87-9c667da8f060\","
      + "\"node\":{"
      + "\"id\":\"e89eaf68-50c1-47f2-b83a-5b51ffa2206e\","
      + "\"referenceId\":\"13037147-1769-4735-90a7-b9b310d128b8\","
      + "\"refDataFacility\":true"
      + "},"
      + "\"name\":\"Balaka District Hospital\","
      + "\"isFreeTextAllowed\":false"
      + "},"
      + "{"
      + "\"id\":\"c51365aa-ad56-4f5e-a7f4-d1eded160d46\","
      + "\"programId\":\"10845cb9-d365-4aaa-badd-b4fa39c6a26a\","
      + "\"facilityTypeId\":\"ac1d268b-ce10-455f-bf87-9c667da8f060\","
      + "\"node\":{"
      + "\"id\":\"bb551f22-66b1-487a-a87d-9083d9eb8d56\","
      + "\"referenceId\":\"f87480a8-5f06-4152-8e9d-6b53762a917e\","
      + "\"refDataFacility\":false"
      + "},"
      + "\"name\":\"CHW\","
      + "\"isFreeTextAllowed\":true"
      + "},"
      + "{"
      + "\"id\":\"a44fcfa4-edd8-45a7-a578-fcf5ae7791b3\","
      + "\"programId\":\"10845cb9-d365-4aaa-badd-b4fa39c6a26a\","
      + "\"facilityTypeId\":\"ac1d268b-ce10-455f-bf87-9c667da8f060\","
      + "\"node\":{"
      + "\"id\":\"e89eaf68-50c1-47f2-b83a-5b51ffa2206e\","
      + "\"referenceId\":\"13037147-1769-4735-90a7-b9b310d128b8\","
      + "\"refDataFacility\":true"
      + "},"
      + "\"name\":\"Balaka District Hospital\","
      + "\"isFreeTextAllowed\":false"
      + "}"
      + "],"
      + "\"pageable\":{"
      + "\"sort\":["
      + "],"
      + "\"pageNumber\":0,"
      + "\"pageSize\":2147483647,"
      + "\"offset\":0,"
      + "\"unpaged\":false,"
      + "\"paged\":true"
      + "},"
      + "\"totalElements\":3,"
      + "\"last\":false,"
      + "\"totalPages\":1,"
      + "\"first\":true,"
      + "\"sort\":["
      + "],"
      + "\"numberOfElements\":3,"
      + "\"size\":2147483647,"
      + "\"number\":0,"
      + "\"empty\":false"
      + "}";

  @Before
  public void setUp() {
    wireMockRule.stubFor(
        get(urlPathEqualTo("/api/validDestinations"))
            .withQueryParam("facilityId", equalTo(fromFacilityId.toString()))
            .withQueryParam("programId", equalTo(programId.toString()))
            .withQueryParam("page", equalTo(String.valueOf(0)))
            .withQueryParam("size", equalTo(String.valueOf(Integer.MAX_VALUE)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(VALID_DESTINATIONS_RESPONSE))
    );

    wireMockRule.stubFor(
        get(urlPathEqualTo("/api/validDestinations"))
            .withQueryParam("facilityId", equalTo(nonMatchingId.toString()))
            .withQueryParam("programId", equalTo(nonMatchingId.toString()))
            .withQueryParam("page", equalTo(String.valueOf(0)))
            .withQueryParam("size", equalTo(String.valueOf(Integer.MAX_VALUE)))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(MOCK_EMPTY_PAGE))
    );
  }

  @Test
  public void shouldFindValidDestination() {
    UUID expectedValidDestinationId = UUID.fromString("8059d138-f34e-4286-bc79-4b8f60aecca6");

    Optional<ValidSourceDestinationDto> searchResult = validDestinationsStockManagementService
        .search(programId, fromFacilityId, toFacilityId);

    assertTrue(searchResult.isPresent());
    assertEquals(expectedValidDestinationId, searchResult.get().getId());
  }

  @Test
  public void shouldNotFindValidDestination() {
    Optional<ValidSourceDestinationDto> searchResult = validDestinationsStockManagementService
        .search(nonMatchingId, nonMatchingId, nonMatchingId);

    assertFalse(searchResult.isPresent());
  }
}
