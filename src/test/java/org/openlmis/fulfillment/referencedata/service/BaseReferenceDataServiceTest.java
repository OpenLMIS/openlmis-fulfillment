package org.openlmis.fulfillment.referencedata.service;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseReferenceDataServiceTest<T> {
  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String ACCESS_TOKEN = "access_token=" + TOKEN;
  private static final String AUTHORIZATION_URL = "http://localhost/auth/oauth/token";

  @Mock
  private RestTemplate restTemplate;
  @Captor
  private ArgumentCaptor<URI> uriCaptor;

  @Before
  public void setUp() throws Exception {
    mockAuth();
  }

  @Test
  public void shouldFindById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();
    T instance = generateInstance();
    ResponseEntity<T> response = mock(ResponseEntity.class);

    // when
    when(response.getBody()).thenReturn(instance);
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), eq(null), eq(service.getResultClass())
    )).thenReturn(response);

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), eq(null), eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl() + id + "?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(instance));
  }

  private BaseReferenceDataService<T> prepareService() {
    BaseReferenceDataService<T> service = getService();
    service.setRestTemplate(restTemplate);

    ReflectionTestUtils.setField(service, "clientId", "trusted-client");
    ReflectionTestUtils.setField(service, "clientSecret", "secret");
    ReflectionTestUtils.setField(service, "referenceDataUrl", "http://localhost/referencedata");
    ReflectionTestUtils.setField(service, "authorizationUrl", AUTHORIZATION_URL);

    return service;
  }

  private void mockAuth() {
    ResponseEntity<Object> response = mock(ResponseEntity.class);
    Map<String, String> body = ImmutableMap.of("access_token", TOKEN);

    URI auth = URI.create(AUTHORIZATION_URL + "?grant_type=client_credentials");
    when(restTemplate.exchange(
        eq(auth), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)
    )).thenReturn(response);

    when(response.getBody()).thenReturn(body);
  }

  abstract BaseReferenceDataService<T> getService();

  abstract T generateInstance();

}
