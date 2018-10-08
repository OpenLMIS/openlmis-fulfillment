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

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.CsvFileColumn;
import org.openlmis.fulfillment.domain.CsvFileTemplate;
import org.openlmis.fulfillment.domain.CsvTemplateType;
import org.openlmis.fulfillment.service.CsvFileTemplateService;
import org.openlmis.fulfillment.web.util.CsvFileTemplateDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class CsvFileTemplateControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/csvFileTemplates";
  private static final String ORDER = "ORDER";

  private CsvFileTemplate csvFileTemplate = new CsvFileTemplate();
  private CsvFileTemplateDto csvFileTemplateDto;

  @MockBean
  private CsvFileTemplateService csvFileTemplateService;

  @Before
  public void setUp() {
    csvFileTemplate.setId(UUID.randomUUID());
    csvFileTemplate.setFilePrefix("prefix");
    csvFileTemplate.setHeaderInFile(false);
    csvFileTemplate.setTemplateType(CsvTemplateType.ORDER);
    csvFileTemplate.setCsvFileColumns(new ArrayList<>());

    given(csvFileTemplateRepository.save(any(CsvFileTemplate.class)))
        .willAnswer(new SaveAnswer<CsvFileTemplate>());
  }

  // POST /api/orderFileTemplates

  @Test
  public void shouldNotCreateNewOrderFileTemplate() {
    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(405);
  }

  // PUT /api/orderFileTemplates

  @Test
  public void shouldUpdateOrderFileTemplate() {
    // given
    CsvFileTemplate originalTemplate = new CsvFileTemplate();
    originalTemplate.setCsvFileColumns(new ArrayList<>());
    originalTemplate.setId(csvFileTemplate.getId());

    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);

    when(csvFileTemplateService.getCsvFileTemplate(CsvTemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    CsvFileTemplateDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(CsvFileTemplateDto.class);

    // then
    verify(csvFileTemplateRepository, atLeastOnce()).save(eq(originalTemplate));
    assertEquals(csvFileTemplateDto.getFilePrefix(), result.getFilePrefix());
    assertEquals(csvFileTemplateDto.getHeaderInFile(), result.getHeaderInFile());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenOrderFileColumnContainsWrongFormat() {
    CsvFileColumn csvFileColumn = new CsvFileColumn();
    csvFileColumn.setDataFieldLabel("label");
    csvFileColumn.setColumnLabel("label");
    csvFileColumn.setNested("nested");
    csvFileColumn.setKeyPath("key");
    csvFileColumn.setRelated("yes");
    csvFileColumn.setRelatedKeyPath("yes");
    csvFileColumn.setId(UUID.randomUUID());
    csvFileColumn.setFormat("dddd-mmm-yy");
    csvFileColumn.setInclude(true);
    csvFileColumn.setPosition(1);
    csvFileColumn.setOpenLmisField(true);
    csvFileTemplate.getCsvFileColumns().add(csvFileColumn);

    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateOrderFileTemplate() {
    denyUserAllRights();
    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenIdNotMatching() {
    // given
    CsvFileTemplate originalTemplate = new CsvFileTemplate();
    originalTemplate.setCsvFileColumns(new ArrayList<>());
    originalTemplate.setId(getNonMatchingUuid(csvFileTemplate.getId()));

    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);

    when(csvFileTemplateService.getCsvFileTemplate(CsvTemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .as(CsvFileTemplateDto.class);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenIdNull() {
    // given
    CsvFileTemplate originalTemplate = new CsvFileTemplate();
    originalTemplate.setCsvFileColumns(new ArrayList<>());
    originalTemplate.setId(csvFileTemplate.getId());

    csvFileTemplateDto = CsvFileTemplateDto.newInstance(csvFileTemplate);
    csvFileTemplateDto.setId(null);

    when(csvFileTemplateService.getCsvFileTemplate(CsvTemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(csvFileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .as(CsvFileTemplateDto.class);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/csvFileTemplates

  @Test
  public void shouldReturnOrderFileTemplate() {
    given(csvFileTemplateService.getCsvFileTemplate(CsvTemplateType.ORDER))
        .willReturn(csvFileTemplate);

    CsvFileTemplateDto result = restAssured.given()
        .queryParam("templateType", ORDER)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(CsvFileTemplateDto.class);

    assertEquals(csvFileTemplate.getId(),result.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotReturnOrderFileTemplateIfItDoesNotExist() {
    given(csvFileTemplateService.getOrderFileTemplate()).willReturn(null);

    restAssured.given()
        .queryParam("templateType", ORDER)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
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
        .queryParam("templateType", ORDER)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
