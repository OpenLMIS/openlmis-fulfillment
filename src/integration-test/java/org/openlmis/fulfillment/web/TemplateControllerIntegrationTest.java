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
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.TemplateRepository;
import org.openlmis.fulfillment.web.util.TemplateDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings({"PMD.UnusedPrivateField"})
public class TemplateControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/reports/templates/fulfillment";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String ACCESS_TOKEN = "access_token";
  private static final String TEMPLATE_CONTROLLER_TEST = "TemplateControllerIntegrationTest";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");

  @MockBean
  private TemplateRepository templateRepository;

  private Template template = new Template();
  private TemplateDto templateDto;
  private Integer currentInstanceNumber;

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    currentInstanceNumber = 0;

    template.setId(UUID.randomUUID());
    template.setName(TEMPLATE_CONTROLLER_TEST + generateInstanceNumber());
    templateDto = TemplateDto.newInstance(template);

    given(templateRepository.findOne(template.getId())).willReturn(template);
    given(templateRepository.exists(template.getId())).willReturn(true);

    given(templateRepository.save(any(Template.class))).willAnswer(new SaveAnswer<Template>());
    given(templateRepository.findAll()).willReturn(Lists.newArrayList(template));
  }

  private Integer generateInstanceNumber() {
    currentInstanceNumber += 1;
    return currentInstanceNumber;
  }

  @Test
  public void shouldAddReportTemplate() throws IOException {
    ClassPathResource podReport = new ClassPathResource("jasperTemplates/proofOfDelivery.jrxml");

    try (InputStream podStream = podReport.getInputStream()) {
      restAssured.given()
          .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
          .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
          .multiPart("file", podReport.getFilename(), podStream)
          .formParam("name", TEMPLATE_CONTROLLER_TEST)
          .formParam("description", TEMPLATE_CONTROLLER_TEST)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(200);
    }

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnBadRequestWhenTemplateExist() throws IOException {
    ClassPathResource podReport = new ClassPathResource("jasperTemplates/proofOfDelivery.jrxml");

    given(templateRepository.findByName(TEMPLATE_CONTROLLER_TEST)).willReturn(new Template());
    try (InputStream podStream = podReport.getInputStream()) {
      restAssured.given()
          .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
          .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
          .multiPart("file", podReport.getFilename(), podStream)
          .formParam("name", TEMPLATE_CONTROLLER_TEST)
          .formParam("description", TEMPLATE_CONTROLLER_TEST)
          .when()
          .post(RESOURCE_URL)
          .then()
          .statusCode(400);
    }

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldDeleteTemplate() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", template.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(204);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotDeleteNonexistentTemplate() {
    given(templateRepository.findOne(template.getId())).willReturn(null);
    given(templateRepository.exists(template.getId())).willReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", template.getId())
        .when()
        .delete(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateTemplate() {

    templateDto.setDescription(TEMPLATE_CONTROLLER_TEST);

    TemplateDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", template.getId())
        .body(templateDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TemplateDto.class);

    assertEquals(response.getDescription(), TEMPLATE_CONTROLLER_TEST);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewTemplateIfDoesNotExist() {
    given(templateRepository.findOne(template.getId())).willReturn(template);
    given(templateRepository.exists(template.getId())).willReturn(true);

    templateDto.setDescription(TEMPLATE_CONTROLLER_TEST);

    TemplateDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ID)
        .body(templateDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TemplateDto.class);

    assertEquals(response.getDescription(), TEMPLATE_CONTROLLER_TEST);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllTemplates() {
    TemplateDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(TemplateDto[].class);

    Iterable<TemplateDto> templates = Arrays.asList(response);
    assertTrue(templates.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenTemplate() {

    TemplateDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", template.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(TemplateDto.class);

    assertEquals(template.getId(), response.getId());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentTemplate() {
    given(templateRepository.findOne(template.getId())).willReturn(null);
    given(templateRepository.exists(template.getId())).willReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", template.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
