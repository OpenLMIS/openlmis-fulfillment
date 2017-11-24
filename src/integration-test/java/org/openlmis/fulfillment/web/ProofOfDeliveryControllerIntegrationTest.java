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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PROOF_OF_DELIVERY_ALREADY_SUBMITTED;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import com.google.common.collect.Lists;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.TemplateRepository;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingScheduleDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.SupervisoryNodeDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.TooManyMethods")
public class ProofOfDeliveryControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/proofOfDeliveries";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  
  private static final String PRINT_URL = ID_URL + "/print";
  private static final String SUBMIT_URL = ID_URL + "/submit";

  private static final String PRINT_POD = "Print POD";
  private static final String CONSISTENCY_REPORT = "Consistency Report";
  private static final UUID ID = UUID.fromString("1752b457-0a4b-4de0-bf94-5a6a8002427e");
  private static final String MESSAGE_KEY = "messageKey";

  @MockBean
  private TemplateRepository templateRepository;

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  private ProofOfDelivery proofOfDelivery = new ProofOfDelivery();
  private ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
  private ProofOfDeliveryLineItem proofOfDeliveryLineItem = new ProofOfDeliveryLineItem();
  private UUID proofOfDeliveryId = UUID.randomUUID();

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    this.setUpBootstrapData();

    OrderableDto product = new OrderableDto();
    product.setId(UUID.randomUUID());

    FacilityDto facility = new FacilityDto();
    facility.setId(UUID.randomUUID());
    facility.setCode("facilityCode");
    facility.setName("facilityName");
    facility.setDescription("facilityDescription");
    facility.setActive(true);
    facility.setEnabled(true);

    SupervisoryNodeDto supervisoryNode = new SupervisoryNodeDto();
    supervisoryNode.setId(UUID.randomUUID());
    supervisoryNode.setCode("NodeCode");
    supervisoryNode.setName("NodeName");
    supervisoryNode.setFacility(facility);

    ProgramDto program = new ProgramDto();
    program.setId(UUID.randomUUID());
    program.setCode("programCode");

    ProcessingPeriodDto period = new ProcessingPeriodDto();
    period.setId(UUID.randomUUID());
    period.setProcessingSchedule(new ProcessingScheduleDto());
    period.setName("periodName");
    period.setStartDate(LocalDate.of(2015, Month.JANUARY, 1));
    period.setEndDate(LocalDate.of(2015, Month.DECEMBER, 31));

    OrderLineItem orderLineItem = new OrderLineItemDataBuilder()
        .withOrderableId(product.getId())
        .build();

    Order order = new OrderDataBuilder()
        .withLineItems(orderLineItem)
        .withShippedStatus()
        .withProgramId(program.getId())
        .withProcessingPeriodId(period.getId())
        .withFacilityId(facility.getId())
        .withSupplyingFacilityId(facility.getId())
        .withRequestingFacilityId(facility.getId())
        .withReceivingFacilityId(facility.getId())
        .build();

    given(orderRepository.findOne(order.getId())).willReturn(order);
    given(orderRepository.exists(order.getId())).willReturn(true);

    proofOfDeliveryLineItem.setId(UUID.randomUUID());
    proofOfDeliveryLineItem.setOrderLineItem(orderLineItem);
    proofOfDeliveryLineItem.setQuantityShipped(100L);
    proofOfDeliveryLineItem.setQuantityReturned(100L);
    proofOfDeliveryLineItem.setQuantityReceived(100L);
    proofOfDeliveryLineItem.setReplacedProductCode("replaced product code");
    proofOfDeliveryLineItem.setNotes("Notes");

    proofOfDelivery.setId(UUID.randomUUID());
    proofOfDelivery.setOrder(order);
    proofOfDelivery.setDeliveredBy("delivered by");
    proofOfDelivery.setReceivedBy("received by");
    proofOfDelivery.setReceivedDate(LocalDate.now());
    proofOfDelivery.setProofOfDeliveryLineItems(new ArrayList<>());
    proofOfDelivery.getProofOfDeliveryLineItems().add(proofOfDeliveryLineItem);

    proofOfDeliveryDto = ProofOfDeliveryDto.newInstance(proofOfDelivery, exporter);

    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId()))
        .willReturn(proofOfDelivery);
    given(proofOfDeliveryRepository.findOne(proofOfDeliveryId)).willReturn(null);
    given(proofOfDeliveryRepository.exists(proofOfDelivery.getId()))
        .willReturn(true);

    given(proofOfDeliveryRepository.save(any(ProofOfDelivery.class)))
        .willAnswer(new SaveAnswer<ProofOfDelivery>());
  }

  @Test
  @Ignore("Current version *.jrxml have relations to different modules (like reference-data)")
  public void shouldPrintProofOfDeliveryToPdf() throws Exception {
    ClassPathResource podReport = new ClassPathResource("reports/podPrint.jrxml");

    Template template = new Template(PRINT_POD, null, null, CONSISTENCY_REPORT, "");

    JasperReport report = JasperCompileManager.compileReport(podReport.getInputStream());
    JRParameter[] jrParameters = report.getParameters();

    if (jrParameters != null && jrParameters.length > 0) {
      template.setTemplateParameters(
          Arrays.stream(jrParameters)
              .filter(p -> !p.isSystemDefined())
              .map(this::createParameter)
              .collect(Collectors.toList())
      );
    }

    given(templateRepository.findByName(PRINT_POD)).willReturn(template);

    restAssured.given()
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotPrintProofOfDeliveryIfTemplateNonExistent() throws Exception {
    // given
    given(templateRepository.findByName(any(String.class))).willReturn(null);

    // when
    restAssured.given()
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(400);

    // then
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateProofOfDelivery() {
    String somebody = "Somebody";
    proofOfDeliveryDto.setDeliveredBy(somebody);

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(proofOfDeliveryDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto.class);

    assertThat(response.getDeliveredBy(), is(equalTo(somebody)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateProofOfDeliveryWhenIsSubmitted() {
    proofOfDelivery.getOrder().setStatus(OrderStatus.RECEIVED);

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(proofOfDeliveryDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400)
        .extract().path("messageKey");

    assertThat(response, is(equalTo(ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldCreateNewProofOfDeliveryIfDoesNotExist() {
    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId())).willReturn(null);
    given(proofOfDeliveryRepository.exists(proofOfDelivery.getId())).willReturn(false);

    String somebody = "Somebody";
    proofOfDeliveryDto.setDeliveredBy(somebody);

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", ID)
        .body(proofOfDeliveryDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto.class);

    assertThat(response.getDeliveredBy(), is(equalTo(somebody)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetAllProofOfDeliveries() {
    given(proofOfDeliveryRepository.findAll()).willReturn(Lists.newArrayList(proofOfDelivery));

    ProofOfDeliveryDto[] response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto[].class);

    Iterable<ProofOfDeliveryDto> proofOfDeliveries = Arrays.asList(response);
    assertTrue(proofOfDeliveries.iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetChosenProofOfDelivery() {

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto.class);

    assertTrue(proofOfDeliveryRepository.exists(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentProofOfDelivery() {
    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId())).willReturn(null);
    given(proofOfDeliveryRepository.exists(proofOfDelivery.getId())).willReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSubmitValidObject() {
    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .post(SUBMIT_URL)
        .then()
        .statusCode(200)
        .extract().as(ProofOfDeliveryDto.class);

    assertThat(response.getOrder().getStatus(), is(OrderStatus.RECEIVED));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSubmitIfObjectDoesNotExist() {
    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId())).willReturn(null);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .post(SUBMIT_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSubmitIfObjectIsNotValid() {
    proofOfDelivery.setDeliveredBy(null);

    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId())).willReturn(proofOfDelivery);

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .post(SUBMIT_URL)
        .then()
        .statusCode(400)
        .extract().path("[0].messageKey");

    assertThat(response, is(equalTo(VALIDATION_ERROR_MUST_CONTAIN_VALUE)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSubmitIfObjectHasBeenSubmittedEarilier() {
    proofOfDelivery.getOrder().setStatus(OrderStatus.RECEIVED);

    given(proofOfDeliveryRepository.findOne(proofOfDelivery.getId())).willReturn(proofOfDelivery);

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .post(SUBMIT_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PROOF_OF_DELIVERY_ALREADY_SUBMITTED)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetAllRequestIfUserHasNoRight() {
    given(proofOfDeliveryRepository.findAll()).willReturn(Lists.newArrayList(proofOfDelivery));
    
    denyUserAllRights();

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectGetRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectUpdateRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(proofOfDeliveryDto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectPrintRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured
        .given()
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldRejectSubmitRequestIfUserHasNoRight() {
    denyUserAllRights();

    String response = restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .post(SUBMIT_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(equalTo(ERROR_PERMISSION_MISSING)));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private TemplateParameter createParameter(JRParameter jrParameter) {
    TemplateParameter templateParameter = new TemplateParameter();
    templateParameter.setName(jrParameter.getName());
    templateParameter.setDisplayName(jrParameter.getPropertiesMap().getProperty("displayName"));
    templateParameter.setDescription(jrParameter.getDescription());
    templateParameter.setDataType(jrParameter.getValueClassName());

    String selectSql = jrParameter.getPropertiesMap().getProperty("selectSql");
    if (isNotBlank(selectSql)) {
      templateParameter.setSelectSql(selectSql);
    }

    if (jrParameter.getDefaultValueExpression() != null) {
      templateParameter.setDefaultValue(jrParameter.getDefaultValueExpression()
          .getText().replace("\"", "").replace("\'", ""));
    }

    return templateParameter;
  }
}
