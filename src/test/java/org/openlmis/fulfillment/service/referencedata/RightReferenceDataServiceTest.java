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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public class RightReferenceDataServiceTest extends BaseReferenceDataServiceTest<RightDto> {

  @Override
  protected BaseReferenceDataService<RightDto> getService() {
    return new RightReferenceDataService();
  }

  @Override
  RightDto generateInstance() {
    return new RightDto();
  }

  @Test
  public void shouldFindRightByName() throws Exception {
    // given
    String name = "testRight";

    RightDto rightDto = generateInstance();
    rightDto.setName(name);

    RightDto[] rights = new RightDto[]{rightDto};
    RightReferenceDataService service = (RightReferenceDataService) prepareService();
    ResponseEntity<RightDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(rights);

    RightDto right = service.findRight(name);

    // then
    verify(restTemplate).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl()
        + "search?" + ACCESS_TOKEN + "&name=" + name;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(right.getName(), is(equalTo(name)));
  }

  @Test
  public void shouldReturnNullIfRightCannotBeFound() throws Exception {
    // given
    String name = "testRight";

    RightDto[] rights = new RightDto[0];
    RightReferenceDataService service = (RightReferenceDataService) prepareService();
    ResponseEntity<RightDto[]> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(rights);

    RightDto right = service.findRight(name);

    // then
    verify(restTemplate).getForEntity(
        uriCaptor.capture(), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl()
        + "search?" + ACCESS_TOKEN + "&name=" + name;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(right, is(nullValue()));
  }

}
