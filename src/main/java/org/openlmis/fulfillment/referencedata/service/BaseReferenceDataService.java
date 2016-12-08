package org.openlmis.fulfillment.referencedata.service;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseReferenceDataService<T> {
  private static final String ACCESS_TOKEN = "access_token";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Value("${auth.server.clientId}")
  private String clientId;

  @Value("${auth.server.clientSecret}")
  private String clientSecret;

  @Value("${referencedata.url}")
  private String referenceDataUrl;

  @Value("${auth.server.authorizationUrl}")
  private String authorizationUrl;

  private RestOperations restTemplate = new RestTemplate();

  /**
   * Return one object from Reference data service.
   *
   * @param id UUID of requesting object.
   * @return Requesting reference data object.
   */
  public T findOne(UUID id) {
    String url = getReferenceDataUrl() + getUrl() + id;

    Map<String, String> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());

    try {
      ResponseEntity<T> responseEntity = restTemplate.exchange(
          buildUri(url, params), HttpMethod.GET, null, getResultClass());
      return responseEntity.getBody();
    } catch (HttpStatusCodeException ex) {
      // rest template will handle 404 as an exception, instead of returning null
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        logger.warn("{} with id {} does not exist. ", getResultClass().getSimpleName(), id);
        return null;
      } else {
        throw buildRefDataException(ex);
      }
    }
  }

  /**
   * Return all reference data T objects.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return all reference data T objects.
   */
  Collection<T> findAll(String resourceUrl, Map<String, Object> parameters) {
    return findAllWithMethod(resourceUrl, parameters, null, HttpMethod.GET);
  }

  /**
   * Return all reference data T objects that need to be retrieved with POST request.
   *
   * @param resourceUrl   Endpoint url.
   * @param uriParameters Map of query parameters.
   * @param payload       body to include with the outgoing request.
   * @return all reference data T objects.
   */
  Collection<T> postFindAll(String resourceUrl, Map<String, Object> uriParameters,
                                   Map<String, Object> payload) {
    return findAllWithMethod(resourceUrl, uriParameters, payload, HttpMethod.POST);
  }

  private Collection<T> findAllWithMethod(String resourceUrl, Map<String, Object> uriParameters,
                                          Map<String, Object> payload, HttpMethod method) {
    String url = getReferenceDataUrl() + getUrl() + resourceUrl;

    Map<String, Object> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());
    params.putAll(uriParameters);

    try {
      ResponseEntity<T[]> responseEntity;
      if (HttpMethod.GET == method) {
        responseEntity = restTemplate.getForEntity(buildUri(url, params), getArrayResultClass());
      } else {
        responseEntity = restTemplate.postForEntity(buildUri(url, params), payload,
            getArrayResultClass());
      }

      return new ArrayList<>(Arrays.asList(responseEntity.getBody()));
    } catch (HttpStatusCodeException ex) {
      throw buildRefDataException(ex);
    }
  }

  <P> P get(Class<P> type, String resourceUrl, Map<String, Object> parameters) {
    String url = getReferenceDataUrl() + getUrl() + resourceUrl;
    Map<String, Object> params = new HashMap<>();
    params.put(ACCESS_TOKEN, obtainAccessToken());
    params.putAll(parameters);

    ResponseEntity<P> response = restTemplate.getForEntity(buildUri(url, params), type);

    return response.getBody();
  }

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  void setRestTemplate(RestOperations template) {
    this.restTemplate = template;
  }

  String getReferenceDataUrl() {
    return referenceDataUrl;
  }

  private String obtainAccessToken() {
    String plainCreds = clientId + ":" + clientSecret;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);

    HttpEntity<String> request = new HttpEntity<>(headers);

    Map<String, Object> params = new HashMap<>();
    params.put("grant_type", "client_credentials");

    ResponseEntity<?> response = restTemplate.exchange(
        buildUri(authorizationUrl, params), HttpMethod.POST, request, Object.class);


    return ((Map<String, String>) response.getBody()).get(ACCESS_TOKEN);
  }

  private URI buildUri(String url, Map<String, ?> params) {
    UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uri(URI.create(url));

    params.entrySet().forEach(e -> builder.queryParam(e.getKey(), e.getValue()));

    return builder.build(true).toUri();
  }

  private ReferenceDataRetrievalException buildRefDataException(HttpStatusCodeException ex) {
    return new ReferenceDataRetrievalException(
        getResultClass().getSimpleName(), ex.getStatusCode(), ex.getResponseBodyAsString()
    );
  }
}
