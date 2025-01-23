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

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.openlmis.fulfillment.i18n.MessageKeys.MUST_CONTAIN_VALUE;
import static org.openlmis.fulfillment.i18n.MessageKeys.PERMISSIONS_MISSING;
import static org.openlmis.fulfillment.i18n.MessageKeys.PERMISSION_MISSING;
import static org.openlmis.fulfillment.i18n.MessageKeys.PROOF_OF_DELIVERY_ALREADY_CONFIRMED;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.common.collect.ImmutableSet;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.sf.jasperreports.engine.JRParameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openlmis.fulfillment.ProofOfDeliveryDataBuilder;
import org.openlmis.fulfillment.ProofOfDeliveryLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;
import org.openlmis.fulfillment.domain.VersionEntityReference;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.FulfillmentNotificationService;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.PageDto;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.ProofOfDeliveryService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PermissionStringDto;
import org.openlmis.fulfillment.service.referencedata.PermissionStrings;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.util.Pagination;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryLineItemDto;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SuppressWarnings("PMD.TooManyMethods")
public class ProofOfDeliveryControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/proofsOfDelivery";
  private static final String ID_URL = RESOURCE_URL + "/{id}";
  private static final String PRINT_URL = ID_URL + "/print";
  private static final String AUDIT_LOG_URL = ID_URL + "/auditLog";

  private static final String MESSAGE_KEY = "messageKey";
  private static final String PARAM_PAGE = "page";
  private static final String PARAM_SIZE = "size";

  @MockBean
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @MockBean
  private ProofOfDeliveryService proofOfDeliveryService;

  @MockBean
  private ShipmentRepository shipmentRepository;

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private StockEventBuilder stockEventBuilder;

  @MockBean
  private StockEventStockManagementService stockEventStockManagementService;

  @SpyBean
  private PermissionService permissionService;

  @MockBean
  private PermissionStrings.Handler permissionStringsHandler;

  @MockBean
  private FulfillmentNotificationService fulfillmentNotificationService;

  @MockBean
  private JasperReportsViewService jasperReportsViewService;

  @SpyBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @Value("${service.url}")
  private String serviceUrl;

  private ProofOfDeliveryLineItem lineItem = new ProofOfDeliveryLineItemDataBuilder().build();
  private ProofOfDelivery proofOfDelivery = new ProofOfDeliveryDataBuilder()
      .withLineItems(singletonList(lineItem))
      .build();
  private Pageable pageable = PageRequest.of(0, 10);
  private List<OrderableDto> orderables = new ArrayList<>();
  private OrderableDto orderableDto;

  @Before
  public void setUp() {
    given(proofOfDeliveryRepository.findById(proofOfDelivery.getId()))
        .willReturn(Optional.of(proofOfDelivery));
    given(proofOfDeliveryRepository.existsById(proofOfDelivery.getId())).willReturn(true);
    given(proofOfDeliveryRepository.save(any(ProofOfDelivery.class)))
        .willAnswer(new SaveAnswer<>());
    given(shipmentRepository.findById(proofOfDelivery.getShipment().getId()))
        .willReturn(Optional.of(proofOfDelivery.getShipment()));

    given(permissionService.getPermissionStrings(INITIAL_USER_ID))
        .willReturn(permissionStringsHandler);

    given(permissionStringsHandler.get())
        .willReturn(ImmutableSet.of(PermissionStringDto.create(
            PODS_MANAGE, proofOfDelivery.getReceivingFacilityId(), proofOfDelivery.getProgramId()
        )));

    orderables = proofOfDelivery.getLineItems()
        .stream()
        .map(item -> new OrderableDataBuilder()
            .withId(item.getOrderable().getId())
            .withVersionNumber(item.getOrderable().getVersionNumber())
            .build())
        .collect(Collectors.toList());

    given(orderableReferenceDataService.findByIdentities(anySetOf(VersionEntityReference.class)))
        .willReturn(orderables);
  }

  @Test
  public void shouldGetAllProofsOfDelivery() {
    given(proofOfDeliveryService.search(
        isNull(),
        isNull(),
        any(Pageable.class)))
        .willReturn(Pagination.getPage(singletonList(proofOfDelivery), pageable, 1));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .queryParam(PARAM_PAGE, pageable.getPageNumber())
        .queryParam(PARAM_SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(PageDto.class);

    assertTrue(response.getContent().iterator().hasNext());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(proofOfDeliveryService).search(null, null, pageable);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowPaginationWithZeroSize() {
    Pageable page = PageRequest.of(0, 0);
    restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .queryParam(PARAM_PAGE, page.getPageNumber())
            .queryParam(PARAM_SIZE, page.getPageSize())
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(400);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowPaginationWithoutSize() {
    Pageable page = PageRequest.of(0, 0);
    restAssured.given()
            .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .queryParam(PARAM_PAGE, page.getPageNumber())
            .when()
            .get(RESOURCE_URL)
            .then()
            .statusCode(400);
  }

  @Test
  public void shouldFindProofOfDeliveryBasedOnShipment() {
    given(proofOfDeliveryService.search(
        any(UUID.class),
        isNull(),
        any(Pageable.class)))
        .willReturn(Pagination.getPage(singletonList(proofOfDelivery), pageable, 1));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam("shipmentId", proofOfDelivery.getShipment().getId())
        .queryParam(PARAM_PAGE, pageable.getPageNumber())
        .queryParam(PARAM_SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(PageDto.class);

    assertEquals(0, response.getNumber());
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());

    assertEquals(createDto(), getPageContent(response, ProofOfDeliveryDto.class).get(0));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(proofOfDeliveryService).search(proofOfDelivery.getShipment().getId(), null, pageable);
  }

  @Test
  public void shouldFindProofOfDeliveryBasedOnOrder() {
    given(proofOfDeliveryService.search(
        isNull(),
        any(UUID.class),
        any(Pageable.class)))
        .willReturn(Pagination.getPage(singletonList(proofOfDelivery), pageable, 1));

    PageDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .queryParam("orderId", proofOfDelivery.getShipment().getOrder().getId())
        .queryParam(PARAM_PAGE, pageable.getPageNumber())
        .queryParam(PARAM_SIZE, pageable.getPageSize())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(PageDto.class);

    assertEquals(0, response.getNumber());
    assertEquals(1, response.getContent().size());
    assertEquals(1, response.getNumberOfElements());
    assertEquals(1, response.getTotalElements());
    assertEquals(1, response.getTotalPages());

    assertEquals(createDto(), getPageContent(response, ProofOfDeliveryDto.class).get(0));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    verify(proofOfDeliveryService)
        .search(null, proofOfDelivery.getShipment().getOrder().getId(), pageable);
  }

  @Test
  public void shouldUpdateProofOfDelivery() {
    String somebody = "Somebody";

    ProofOfDeliveryDto dto = createDto();
    dto.setDeliveredBy(somebody);

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(dto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(ProofOfDeliveryDto.class);

    assertThat(response.getDeliveredBy(), is(somebody));
    // Notifications are only sent on POD confirmation
    verify(fulfillmentNotificationService, never())
        .sendPodConfirmedNotification(any(ProofOfDelivery.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSendNotificationOnProofOfDeliveryConfirmation() {
    ProofOfDeliveryDto dto = createDto();
    dto.setStatus(ProofOfDeliveryStatus.CONFIRMED);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(dto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(ProofOfDeliveryDto.class);

    verify(fulfillmentNotificationService).sendPodConfirmedNotification(any(ProofOfDelivery.class));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateProofOfDeliveryIfDoesNotExist() {
    given(proofOfDeliveryRepository.findById(proofOfDelivery.getId())).willReturn(Optional.empty());
    given(proofOfDeliveryRepository.existsById(proofOfDelivery.getId())).willReturn(false);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(createDto())
        .when()
        .put(ID_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotUpdateProofOfDeliveryWhenIsSubmitted() {
    proofOfDelivery = new ProofOfDeliveryDataBuilder().buildAsConfirmed();
    setUp();

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(createDto())
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(PROOF_OF_DELIVERY_ALREADY_CONFIRMED));
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
        .body(createDto())
        .when()
        .put(ID_URL)
        .then()
        .statusCode(403)
        .extract().path(MESSAGE_KEY);

    assertThat(response, is(PERMISSION_MISSING));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldSubmitValidObject() {
    ProofOfDeliveryDto dto = createDto();
    dto.setStatus(ProofOfDeliveryStatus.CONFIRMED);

    given(stockEventBuilder.fromProofOfDelivery(any(ProofOfDelivery.class)))
        .willReturn(Optional.of(new StockEventDto()));

    ProofOfDeliveryDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(dto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(ProofOfDeliveryDto.class);

    assertThat(response.getStatus(), is(ProofOfDeliveryStatus.CONFIRMED));

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

    verify(orderRepository).save(captor.capture());
    verify(stockEventBuilder).fromProofOfDelivery(any(ProofOfDelivery.class));
    verify(stockEventStockManagementService).submit(any(StockEventDto.class));
    assertThat(captor.getValue().getStatus(), is(OrderStatus.RECEIVED));

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotSubmitIfObjectIsNotValid() {
    proofOfDelivery = new ProofOfDeliveryDataBuilder().withoutDeliveredBy().build();
    setUp();

    ProofOfDeliveryDto dto = createDto();
    dto.setStatus(ProofOfDeliveryStatus.CONFIRMED);

    String response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .pathParam("id", proofOfDelivery.getId())
        .body(dto)
        .when()
        .put(ID_URL)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    verifyZeroInteractions(orderRepository, stockEventBuilder, stockEventStockManagementService);
    assertThat(response, is(MUST_CONTAIN_VALUE));
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

    assertTrue(proofOfDeliveryRepository.existsById(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldNotGetNonexistentProofOfDelivery() {
    given(proofOfDeliveryRepository.findById(proofOfDelivery.getId())).willReturn(Optional.empty());
    given(proofOfDeliveryRepository.existsById(proofOfDelivery.getId())).willReturn(false);

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
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(PERMISSIONS_MISSING));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldPrintProofOfDelivery() {
    given(jasperReportsViewService
        .generateReport(any(Template.class), anyMap()))
        .willReturn(new byte[1]);

    restAssured.given()
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);
  }

  @Test
  public void shouldPrintProofOfDeliveryAsPdf() {
    restAssured.given()
        .queryParam("format", "pdf")
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);
  }

  @Test
  public void shouldPrintProofOfDeliveryAsCsv() {
    restAssured.given()
        .queryParam("datasource", "csv")
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);
  }

  @Test
  public void shouldPrintProofOfDeliveryAsXls() {
    restAssured.given()
        .queryParam("format", "xls")
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);
  }

  @Test
  public void shouldPrintProofOfDeliveryAsHtml() {
    restAssured.given()
        .queryParam("format", "html")
        .pathParam("id", proofOfDelivery.getId())
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .when()
        .get(PRINT_URL)
        .then()
        .statusCode(200);
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
        .extract()
        .path(MESSAGE_KEY);

    assertThat(response, is(PERMISSIONS_MISSING));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundIfProofOfDeliveryDoesNotExistForAuditLogEndpoint() {
    given(proofOfDeliveryRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnUnauthorizedIfUserDoesNotHaveRightForAuditLogEndpoint() {
    denyUserAllRights();

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProofOfDeliveryAuditLog() {
    given(proofOfDeliveryRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(proofOfDelivery));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", proofOfDelivery.getId())
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldGetProofOfDeliveryAuditLogForAuthorAndChangedPropertyName() {
    given(proofOfDeliveryRepository.findById(any(UUID.class)))
        .willReturn(Optional.of(proofOfDelivery));

    restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .pathParam("id", proofOfDelivery.getId())
        .queryParam("author", "admin")
        .queryParam("changedPropertyName", "status")
        .when()
        .get(AUDIT_LOG_URL)
        .then()
        .statusCode(200);

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

  private ProofOfDeliveryDto createDto() {
    ProofOfDeliveryDto dto = new ProofOfDeliveryDto();
    orderableDto = orderables.get(0);
    List<ProofOfDeliveryLineItemDto> lineItemsDtos = exportToDto(lineItem, orderableDto);
    dto.setServiceUrl(serviceUrl);
    dto.setLineItems(lineItemsDtos);
    proofOfDelivery.export(dto);

    return dto;
  }

  private List<ProofOfDeliveryLineItemDto> exportToDto(ProofOfDeliveryLineItem lineItem,
      OrderableDto orderable) {
    ProofOfDeliveryLineItemDto lineItemDto = new ProofOfDeliveryLineItemDto();
    lineItemDto.setServiceUrl(serviceUrl);
    lineItem.export(lineItemDto, orderable);

    return singletonList(lineItemDto);
  }

}
