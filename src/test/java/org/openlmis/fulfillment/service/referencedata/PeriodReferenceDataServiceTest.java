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

package org.openlmis.fulfillment.service.referencedata;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.util.DynamicPageTypeReference;
import org.springframework.http.HttpMethod;

public class PeriodReferenceDataServiceTest
    extends BaseReferenceDataServiceTest<ProcessingPeriodDto> {

  private PeriodReferenceDataService service;

  @Override
  protected BaseReferenceDataService<ProcessingPeriodDto> getService() {
    return new PeriodReferenceDataService();
  }

  @Override
  protected ProcessingPeriodDto generateInstance() {
    return new ProcessingPeriodDto();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    service = (PeriodReferenceDataService) prepareService();
  }

  @Test
  public void shouldReturnOrderablesById() {
    ProcessingPeriodDto period = mockPageResponseEntityAndGetDto();

    String startDate = "2018-04-05";
    String endDate = "2018-05-05";
    List<ProcessingPeriodDto> response = service
        .search(LocalDate.parse(startDate), LocalDate.parse(endDate));

    assertThat(response, hasSize(1));
    assertThat(response, hasItems(period));

    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
        refEq(new DynamicPageTypeReference<>(ProcessingPeriodDto.class)));

    URI uri = uriCaptor.getValue();
    assertEquals(serviceUrl + service.getUrl() + "?startDate=" + startDate + "&endDate=" + endDate,
        uri.toString());

    assertAuthHeader(entityCaptor.getValue());
    assertNull(entityCaptor.getValue().getBody());
  }

}
