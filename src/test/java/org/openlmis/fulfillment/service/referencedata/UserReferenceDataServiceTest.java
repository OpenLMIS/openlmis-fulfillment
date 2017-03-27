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
import org.openlmis.fulfillment.util.PageImplRepresentation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserReferenceDataServiceTest extends BaseReferenceDataServiceTest<UserDto> {

  @Override
  protected BaseReferenceDataService<UserDto> getService() {
    return new UserReferenceDataService();
  }

  @Override
  UserDto generateInstance() {
    return new UserDto();
  }

  @Test
  public void shouldFindUserByName() {
    // given
    String name = "userName";

    UserDto userDto = generateInstance();
    userDto.setUsername(name);

    Map<String, Object> payload = new HashMap<>();
    payload.put("username", name);

    UserReferenceDataService service = (UserReferenceDataService) prepareService();
    ResponseEntity response = mock(ResponseEntity.class);

    // when
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), eq(new HttpEntity<>(payload)),
            any(ParameterizedTypeReference.class)))
        .thenReturn(response);

    PageImplRepresentation<UserDto> page = mock(PageImplRepresentation.class);
    when(page.getContent()).thenReturn(Collections.singletonList(userDto));

    when(response.getBody()).thenReturn(page);

    UserDto user = service.findUser(name);

    // then
    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.POST),
            eq(new HttpEntity<>(payload)), any(ParameterizedTypeReference.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + "search?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(user.getUsername(), is(equalTo(name)));
  }

  @Test
  public void shouldReturnNullIfUserCannotBeFound() {
    // given
    String name = "userName";

    UserDto[] users = new UserDto[0];
    ResponseEntity response = mock(ResponseEntity.class);

    Map<String, Object> payload = new HashMap<>();
    payload.put("username", name);

    UserReferenceDataService service = (UserReferenceDataService) prepareService();

    // when
    when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), eq(new HttpEntity<>(payload)),
            any(ParameterizedTypeReference.class)))
            .thenReturn(response);

    PageImplRepresentation<UserDto> page = mock(PageImplRepresentation.class);
    when(page.getContent()).thenReturn(Collections.emptyList());

    when(response.getBody()).thenReturn(page);


    UserDto user = service.findUser(name);

    // then
    verify(restTemplate).exchange(uriCaptor.capture(), eq(HttpMethod.POST),
            eq(new HttpEntity<>(payload)), any(ParameterizedTypeReference.class));

    URI uri = uriCaptor.getValue();
    String url = service.getServiceUrl() + service.getUrl() + "search?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(user, is(nullValue()));
  }

}
