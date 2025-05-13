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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.service.FileTemplateService;
import org.openlmis.fulfillment.web.util.FileTemplateDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class FileTemplateControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/fileTemplates";
  private static final String ORDER = "ORDER";
  private static final String TEMPLATE_TYPE = "templateType";

  private FileTemplate fileTemplate = new FileTemplate();
  private FileTemplateDto fileTemplateDto;

  @MockBean
  private FileTemplateService fileTemplateService;

  @Before
  public void setUp() {
    fileTemplate.setId(UUID.randomUUID());
    fileTemplate.setFilePrefix("prefix");
    fileTemplate.setHeaderInFile(false);
    fileTemplate.setTemplateType(TemplateType.ORDER);
    fileTemplate.setFileColumns(new ArrayList<>());

    given(fileTemplateRepository.save(any(FileTemplate.class)))
        .willAnswer(new SaveAnswer<FileTemplate>());
  }

  // POST /api/orderFileTemplates

  @Test
  public void shouldNotCreateNewOrderFileTemplate() {
    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(405);
  }

  // PUT /api/orderFileTemplates

  @Test
  public void shouldUpdateOrderFileTemplate() {
    // given
    FileTemplate originalTemplate = new FileTemplate();
    originalTemplate.setFileColumns(new ArrayList<>());
    originalTemplate.setId(fileTemplate.getId());

    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);

    when(fileTemplateService.getFileTemplate(TemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    FileTemplateDto result = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(FileTemplateDto.class);

    // then
    verify(fileTemplateRepository, atLeastOnce()).save(eq(originalTemplate));
    assertEquals(fileTemplateDto.getFilePrefix(), result.getFilePrefix());
    assertEquals(fileTemplateDto.getHeaderInFile(), result.getHeaderInFile());

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenOrderFileColumnContainsWrongFormat() {
    FileColumn fileColumn = new FileColumn();
    fileColumn.setDataFieldLabel("label");
    fileColumn.setColumnLabel("label");
    fileColumn.setNested("nested");
    fileColumn.setKeyPath("key");
    fileColumn.setRelated("yes");
    fileColumn.setRelatedKeyPath("yes");
    fileColumn.setId(UUID.randomUUID());
    fileColumn.setFormat("dddd-mmm-yy");
    fileColumn.setInclude(true);
    fileColumn.setPosition(1);
    fileColumn.setOpenLmisField(true);
    fileTemplate.getFileColumns().add(fileColumn);

    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateOrderFileTemplate() {
    denyUserAllRights();
    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenIdNotMatching() {
    // given
    FileTemplate originalTemplate = new FileTemplate();
    originalTemplate.setFileColumns(new ArrayList<>());
    originalTemplate.setId(getNonMatchingUuid(fileTemplate.getId()));

    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);

    when(fileTemplateService.getFileTemplate(TemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .as(FileTemplateDto.class);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateOrderFileTemplateWhenIdNull() {
    // given
    FileTemplate originalTemplate = new FileTemplate();
    originalTemplate.setFileColumns(new ArrayList<>());
    originalTemplate.setId(fileTemplate.getId());

    fileTemplateDto = FileTemplateDto.newInstance(fileTemplate);
    fileTemplateDto.setId(null);

    when(fileTemplateService.getFileTemplate(TemplateType.ORDER))
        .thenReturn(originalTemplate);

    // when
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(fileTemplateDto)
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(400)
        .extract()
        .as(FileTemplateDto.class);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  // GET /api/csvFileTemplates

  @Test
  public void shouldReturnOrderFileTemplate() {
    given(fileTemplateService.getFileTemplate(TemplateType.ORDER))
        .willReturn(fileTemplate);

    FileTemplateDto result = restAssured.given()
        .queryParam(TEMPLATE_TYPE, ORDER)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(FileTemplateDto.class);

    assertEquals(fileTemplate.getId(),result.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotReturnOrderFileTemplateIfItDoesNotExist() {
    given(fileTemplateService.getOrderFileTemplate()).willReturn(null);

    restAssured.given()
        .queryParam(TEMPLATE_TYPE, ORDER)
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
        .queryParam(TEMPLATE_TYPE, ORDER)
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
