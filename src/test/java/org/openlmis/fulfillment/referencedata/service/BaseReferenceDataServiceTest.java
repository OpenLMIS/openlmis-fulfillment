package org.openlmis.fulfillment.referencedata.service;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseReferenceDataServiceTest<T> {
  private static final String TOKEN = UUID.randomUUID().toString();
  private static final String ACCESS_TOKEN = "access_token=" + TOKEN;
  private static final String AUTHORIZATION_URL = "http://localhost/auth/oauth/token";

  private static final URI AUTHORIZATION_URI =
      URI.create(AUTHORIZATION_URL + "?grant_type=client_credentials");

  @Mock
  private RestTemplate restTemplate;

  @Captor
  private ArgumentCaptor<URI> uriCaptor;

  @Captor
  private ArgumentCaptor<HttpEntity> entityCaptor;

  @Before
  public void setUp() throws Exception {
    mockAuth();
  }

  @After
  public void tearDown() throws Exception {
    checkAuth();
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

  @Test
  public void shouldReturnNullIfEntityCannotBeFoundById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), eq(null), eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    T found = service.findOne(id);

    // then
    verify(restTemplate).exchange(
        uriCaptor.capture(), eq(HttpMethod.GET), eq(null), eq(service.getResultClass())
    );

    URI uri = uriCaptor.getValue();
    String url = service.getReferenceDataUrl() + service.getUrl() + id + "?" + ACCESS_TOKEN;

    assertThat(uri.toString(), is(equalTo(url)));
    assertThat(found, is(nullValue()));
  }

  @Test(expected = ReferenceDataRetrievalException.class)
  public void shouldThrowExceptionIfThereIsOtherProblemWithFindingById() throws Exception {
    // given
    BaseReferenceDataService<T> service = prepareService();
    UUID id = UUID.randomUUID();

    // when
    when(restTemplate.exchange(
        any(URI.class), eq(HttpMethod.GET), eq(null), eq(service.getResultClass())
    )).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

    service.findOne(id);
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

    when(restTemplate.exchange(
        eq(AUTHORIZATION_URI), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)
    )).thenReturn(response);

    when(response.getBody()).thenReturn(body);
  }

  private void checkAuth() {
    verify(restTemplate, atLeastOnce()).exchange(
        eq(AUTHORIZATION_URI), eq(HttpMethod.POST), entityCaptor.capture(), eq(Object.class)
    );

    List<HttpEntity> entities = entityCaptor.getAllValues();
    for (HttpEntity entity : entities) {
      assertThat(
          entity.getHeaders().get("Authorization"),
          contains("Basic dHJ1c3RlZC1jbGllbnQ6c2VjcmV0")
      );
    }

  }

  abstract BaseReferenceDataService<T> getService();

  abstract T generateInstance();

}
