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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.service.BaseCommunicationService;
import org.openlmis.fulfillment.service.BaseCommunicationServiceTest;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDto;
import org.openlmis.fulfillment.web.stockmanagement.ValidSourceDestinationDtoDataBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

public class ValidDestinationsStockManagementServiceTest extends BaseCommunicationServiceTest {

  private ValidDestinationsStockManagementService service;

  @Override
  protected BaseCommunicationService getService() {
    return new ValidDestinationsStockManagementService();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = (ValidDestinationsStockManagementService) prepareService();
    ReflectionTestUtils.setField(service, "stockmanagementUrl", "http://localhost");
  }

  @Test
  public void shouldGetValidDestinations() {
    // given
    ValidSourceDestinationDto destination = new ValidSourceDestinationDtoDataBuilder()
        .build();
    ResponseEntity<ValidSourceDestinationDto[]> response = new ResponseEntity<>(
        new ValidSourceDestinationDto[]{destination}, HttpStatus.OK
    );

    when(restTemplate
        .exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class),
            eq(service.getArrayResultClass())))
        .thenReturn(response);

    // when
    UUID program = UUID.randomUUID();
    UUID facilityType = UUID.randomUUID();
    Collection<ValidSourceDestinationDto> destinations = service
        .getValidDestinations(program, facilityType);

    // then
    assertThat(destinations, hasSize(1));
    assertThat(destinations.iterator().next(), is(destination));

    verify(restTemplate)
        .exchange(uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
            eq(service.getArrayResultClass()));

    String uri = uriCaptor.getValue().toString();
    String url = service.getServiceUrl() + service.getUrl();

    assertThat(
        uri,
        allOf(
            startsWith(url),
            containsString("program=" + program),
            containsString("facilityType=" + facilityType)
        )
    );

    HttpEntity<String> entity = entityCaptor.getValue();
    assertAuthHeader(entity);
    assertThat(entity.getBody(), is(nullValue()));
  }
}
