package org.openlmis.fulfillment.referencedata.service;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;
import org.openlmis.fulfillment.referencedata.model.ResultDto;
import org.openlmis.fulfillment.referencedata.model.UserDto;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

  @Test
  public void shouldCheckUserRight() {
    UUID user = UUID.randomUUID();
    UUID right = UUID.randomUUID();
    UUID program = UUID.randomUUID();
    UUID facility = UUID.randomUUID();

    executeHasRightEndpoint(user, right, program, facility, null, true, true);
    executeHasRightEndpoint(user, right, program, facility, null, false, false);
  }

  @Test
  public void shouldHandleStringResultForHasRightEndpoint() {
    UUID user = UUID.randomUUID();
    UUID right = UUID.randomUUID();
    UUID facility = UUID.randomUUID();

    executeHasRightEndpoint(user, right, null, facility, null, "true", true);
    executeHasRightEndpoint(user, right, null, facility, null, "false", false);
    executeHasRightEndpoint(user, right, null, facility, null, "dsfdsfsdf", false);
  }

  @Test
  public void shouldHandleNumberResultForHasRightEndpoint() {
    UUID user = UUID.randomUUID();
    UUID right = UUID.randomUUID();
    UUID program = UUID.randomUUID();

    executeHasRightEndpoint(user, right, program, null, null, 1, true);
    executeHasRightEndpoint(user, right, program, null, null, 0, false);
    executeHasRightEndpoint(user, right, program, null, null, -1, false);
  }

  @Test
  public void shouldHandleIncorrectResultForHasRightEndpoint() {
    executeHasRightEndpoint(
        UUID.randomUUID(), UUID.randomUUID(), null, null, null, new Object(), false
    );
  }

  private void executeHasRightEndpoint(UUID user, UUID right, UUID program, UUID facility,
                                       UUID warehouse, Object resultValue, boolean expectedValue) {
    // given
    UserReferenceDataService service = (UserReferenceDataService) prepareService();
    ResponseEntity<ResultDto> response = mock(ResponseEntity.class);

    // when
    when(restTemplate.getForEntity(any(URI.class), eq(ResultDto.class)))
        .thenReturn(response);
    when(response.getBody()).thenReturn(new ResultDto<>(resultValue));

    ResultDto result = service.hasRight(user, right, program, facility, warehouse);

    // then
    assertThat(result.getResult(), is(expectedValue));

    verify(restTemplate, atLeastOnce()).getForEntity(
        uriCaptor.capture(), eq(ResultDto.class)
    );

    URI uri = uriCaptor.getValue();
    List<NameValuePair> parse = URLEncodedUtils.parse(uri, "UTF-8");

    assertThat(parse, hasItem(allOf(
        hasProperty("name", is("rightId")), hasProperty("value", is(right.toString())))
    ));

    if (null != program) {
      assertThat(parse, hasItem(allOf(
          hasProperty("name", is("programId")), hasProperty("value", is(program.toString())))
      ));
    }

    if (null != facility) {
      assertThat(parse, hasItem(allOf(
          hasProperty("name", is("facilityId")), hasProperty("value", is(facility.toString())))
      ));
    }
  }

}
