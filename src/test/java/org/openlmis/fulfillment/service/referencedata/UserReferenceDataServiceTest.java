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
import java.util.HashMap;
import java.util.Map;

public class UserReferenceDataServiceTest extends BaseReferenceDataServiceTest<UserDto> {

  @Override
  BaseReferenceDataService<UserDto> getService() {
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

    UserDto[] users = new UserDto[]{userDto};
    UserReferenceDataService service = (UserReferenceDataService) prepareService();
    ResponseEntity<UserDto[]> response = mock(ResponseEntity.class);

    Map<String, Object> payload = new HashMap<>();
    payload.put("username", name);

    // when
    when(restTemplate.postForEntity(any(URI.class), eq(payload), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(users);

    UserDto user = service.findUser(name);

    // then
    verify(restTemplate).postForEntity(
        uriCaptor.capture(), eq(payload), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl() + "search?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(user.getUsername(), is(equalTo(name)));
  }

  @Test
  public void shouldReturnNullIfUserCannotBeFound() {
    // given
    String name = "userName";

    UserDto[] users = new UserDto[0];
    UserReferenceDataService service = (UserReferenceDataService) prepareService();
    ResponseEntity<UserDto[]> response = mock(ResponseEntity.class);

    Map<String, Object> payload = new HashMap<>();
    payload.put("username", name);

    // when
    when(restTemplate.postForEntity(any(URI.class), eq(payload), eq(service.getArrayResultClass())))
        .thenReturn(response);
    when(response.getBody()).thenReturn(users);

    UserDto user = service.findUser(name);

    // then
    verify(restTemplate).postForEntity(
        uriCaptor.capture(), eq(payload), eq(service.getArrayResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl() + "search?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(user, is(nullValue()));
  }

}
