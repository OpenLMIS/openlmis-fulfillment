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

package org.openlmis.fulfillment.web;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.repository.OrderFileTemplateRepository;
import org.openlmis.fulfillment.service.OrderFileTemplateService;
import org.openlmis.fulfillment.web.util.OrderFileTemplateDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.UUID;

import guru.nidi.ramltester.junit.RamlMatchers;

public class OrderFileTemplateControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderFileTemplates";
  private static final String ACCESS_TOKEN = "access_token";

  private OrderFileTemplate orderFileTemplate = new OrderFileTemplate();
  private OrderFileTemplateDto orderFileTemplateDto;

  @MockBean
  private OrderFileTemplateRepository orderFileTemplateRepository;

  @MockBean
  private OrderFileTemplateService orderFileTemplateService;

  @Before
  public void setUp() {
    orderFileTemplate.setId(UUID.randomUUID());
    orderFileTemplate.setFilePrefix("prefix");
    orderFileTemplate.setHeaderInFile(false);
    orderFileTemplate.setOrderFileColumns(new ArrayList<>());


    given(orderFileTemplateRepository.save(any(OrderFileTemplate.class)))
        .willAnswer(new SaveAnswer<OrderFileTemplate>());
  }

  // POST /api/orderFileTemplates

  @Test
  public void shouldNotCreateNewOrderFileTemplate() {
    orderFileTemplateDto = OrderFileTemplateDto.newInstance(orderFileTemplate);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderFileTemplateDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(405);
  }

  // PUT /api/orderFileTemplates

  @Test
  public void shouldUpdateOrderFileTemplate() {
    // given
    OrderFileTemplate originalTemplate = new OrderFileTemplate();
    originalTemplate.setOrderFileColumns(new ArrayList<>());
    orderFileTemplateDto = OrderFileTemplateDto.newInstance(orderFileTemplate);

    when(orderFileTemplateService.getOrderFileTemplate()).thenReturn(originalTemplate);

    // when
    OrderFileTemplateDto result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(OrderFileTemplateDto.class);

    // then
    verify(orderFileTemplateRepository, atLeastOnce()).save(eq(originalTemplate));
    assertEquals(orderFileTemplateDto.getFilePrefix(), result.getFilePrefix());
    assertEquals(orderFileTemplateDto.getHeaderInFile(), result.getHeaderInFile());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenOrderFileColumnContainsWrongFormat() {
    OrderFileColumn orderFileColumn = new OrderFileColumn();
    orderFileColumn.setDataFieldLabel("label");
    orderFileColumn.setColumnLabel("label");
    orderFileColumn.setNested("nested");
    orderFileColumn.setKeyPath("key");
    orderFileColumn.setRelated("yes");
    orderFileColumn.setRelatedKeyPath("yes");
    orderFileColumn.setId(UUID.randomUUID());
    orderFileColumn.setFormat("dddd-mmm-yy");
    orderFileColumn.setInclude(true);
    orderFileColumn.setPosition(1);
    orderFileColumn.setOpenLmisField(true);
    orderFileTemplate.getOrderFileColumns().add(orderFileColumn);

    orderFileTemplateDto = OrderFileTemplateDto.newInstance(orderFileTemplate);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateOrderFileTemplate() {
    denyUserAllRights();
    orderFileTemplateDto = OrderFileTemplateDto.newInstance(orderFileTemplate);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  // GET /api/orderFileTemplates

  @Test
  public void shouldReturnOrderFileTemplate() {
    given(orderFileTemplateService.getOrderFileTemplate()).willReturn(orderFileTemplate);

    OrderFileTemplateDto result = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(OrderFileTemplateDto.class);

    assertEquals(orderFileTemplate.getId(),result.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotReturnOrderFileTemplateIfItDoesNotExist() {
    given(orderFileTemplateService.getOrderFileTemplate()).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToViewOrderFileTemplate() {
    denyUserAllRights();

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
