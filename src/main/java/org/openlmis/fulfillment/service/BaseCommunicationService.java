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

import static org.openlmis.fulfillment.service.request.RequestHelper.createUri;

import org.openlmis.fulfillment.service.request.RequestHeaders;
import org.openlmis.fulfillment.service.request.RequestHelper;
import org.openlmis.fulfillment.service.request.RequestParameters;
import org.openlmis.fulfillment.util.DynamicPageTypeReference;
import org.openlmis.fulfillment.util.PageImplRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public abstract class BaseCommunicationService<T> {
  protected RestOperations restTemplate = new RestTemplate();

  @Autowired
  protected AuthService authService;

  protected abstract String getServiceUrl();

  protected abstract String getUrl();

  protected abstract Class<T> getResultClass();

  protected abstract Class<T[]> getArrayResultClass();

  protected URI buildUri(String url) {
    return createUri(url);
  }

  protected URI buildUri(String url, Map<String, Object> params) {
    return createUri(url, RequestParameters.of(params));
  }

  public void setRestTemplate(RestOperations template) {
    this.restTemplate = template;
  }

  /**
   * Return all reference data T objects.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @return all reference data T objects.
   */
  protected Collection<T> findAll(String resourceUrl, Map<String, Object> parameters) {
    return findAllWithMethod(resourceUrl, parameters, null, HttpMethod.GET);
  }

  protected Collection<T> findAllWithMethod(String resourceUrl, Map<String, Object> uriParameters,
                                          Map<String, Object> payload, HttpMethod method) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    Map<String, Object> params = new HashMap<>();
    params.putAll(uriParameters);

    try {
      ResponseEntity<T[]> responseEntity = restTemplate.exchange(buildUri(url, params),
          method, createEntity(payload), getArrayResultClass());

      return new ArrayList<>(Arrays.asList(responseEntity.getBody()));
    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  /**
   * Return all reference data T objects for Page that need to be retrieved with POST request.
   *
   * @param resourceUrl Endpoint url.
   * @param parameters  Map of query parameters.
   * @param payload     body to include with the outgoing request.
   * @return Page of reference data T objects.
   */
  protected Page<T> getPage(String resourceUrl, Map<String, Object> parameters, Object payload) {
    return getPage(resourceUrl, parameters, payload, HttpMethod.POST, getResultClass());
  }

  protected <P> Page<P> getPage(String resourceUrl, Map<String, Object> parameters, Object payload,
                                HttpMethod method, Class<P> type) {
    String url = getServiceUrl() + getUrl() + resourceUrl;

    try {
      ResponseEntity<PageImplRepresentation<P>> response = restTemplate.exchange(
              buildUri(url, parameters),
              method,
              createEntity(payload),
              new DynamicPageTypeReference<>(type)
      );
      return response.getBody();

    } catch (HttpStatusCodeException ex) {
      throw buildDataRetrievalException(ex);
    }
  }

  protected DataRetrievalException buildDataRetrievalException(HttpStatusCodeException ex) {
    return new DataRetrievalException(getResultClass().getSimpleName(), ex);
  }

  protected <E> HttpEntity<E> createEntity(E payload) {
    if (payload == null) {
      return createEntity();
    } else {
      return RequestHelper.createEntity(payload, createHeadersWithAuth());
    }
  }

  protected  <E> HttpEntity<E> createEntity() {
    return RequestHelper.createEntity(createHeadersWithAuth());
  }

  private RequestHeaders createHeadersWithAuth() {
    return RequestHeaders.init().setAuth(authService.obtainAccessToken());
  }

  protected <T> ResponseEntity<T> runWithTokenRetry(HttpTask<T> task) {
    try {
      return task.run();
    } catch (HttpStatusCodeException ex) {
      if (HttpStatus.UNAUTHORIZED == ex.getStatusCode()) {
        // the token has (most likely) expired - clear the cache and retry once
        authService.clearTokenCache();
        return task.run();
      }
      throw ex;
    }
  }

  protected interface HttpTask<T> {
    ResponseEntity<T> run() throws HttpStatusCodeException;
  }
}
