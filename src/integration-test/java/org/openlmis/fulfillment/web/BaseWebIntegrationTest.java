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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.BDDMockito.given;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.Lists;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.TemplateType;
import org.openlmis.fulfillment.repository.FileColumnRepository;
import org.openlmis.fulfillment.repository.FileTemplateRepository;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test-run")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public abstract class BaseWebIntegrationTest {

  protected static final String ID = "id";
  protected static final String MESSAGE_KEY = "messageKey";
  protected static final UUID INITIAL_USER_ID =
      UUID.fromString("35316636-6264-6331-2d34-3933322d3462");
  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  protected static final String REFERENCEDATA_API_USERS = "/api/users/";
  protected static final String REFERENCEDATA_API_RIGHTS = "/api/rights/";
  protected static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml").ignoringXheaders();
  protected static final String UUID_REGEX =
      "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
  protected static final String CONTENT_TYPE = "Content-Type";
  protected static final String APPLICATION_JSON = "application/json";
  protected static final String FACILITY_ID = "1d5bdd9c-8702-11e6-ae22-56b6b6499611";
  protected static final String ORDER_ID = "orderId";

  private static final String MOCK_CHECK_RESULT = "{"
      + "  \"aud\": [\n"
      + "    \"auth\",\n"
      + "    \"example\",\n"
      + "    \"requisition\",\n"
      + "    \"notification\",\n"
      + "    \"referencedata\",\n"
      + "    \"fulfillment\"\n"
      + "  ],\n"
      + "  \"user_name\": \"admin\",\n"
      + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\",\n"
      + "  \"scope\": [\n"
      + "    \"read\",\n"
      + "    \"write\"\n"
      + "  ],\n"
      + "  \"exp\": 1474500343,\n"
      + "  \"authorities\": [\n"
      + "    \"USER\",\n"
      + "    \"ADMIN\"\n"
      + "  ],\n"
      + "  \"client_id\": \"trusted-client\"\n"
      + "}";
  private static final String MOCK_TOKEN_REQUEST_RESPONSE = "{"
      + "  \"access_token\": \"418c89c5-7f21-4cd1-a63a-38c47892b0fe\",\n"
      + "  \"token_type\": \"bearer\",\n"
      + "  \"expires_in\": 847,\n"
      + "  \"scope\": \"read write\",\n"
      + "  \"referenceDataUserId\": \"35316636-6264-6331-2d34-3933322d3462\"\n"
      + "}";
  private static final String MOCK_FIND_PROGRAM_RESULT = "{"
      + " \"id\":\"5c5a6f68-8658-11e6-ae22-56b6b6499611\","
      + " \"code\":\"Program Code\","
      + " \"name\":\"Program Name\","
      + " \"active\": true,"
      + " \"showNonFullSupplyTab\": false,"
      + " \"periodsSkippable\":true"
      + "}";

  private static final String MOCK_FIND_FACILITY_RESULT = "{"
      + " \"id\":\"" + FACILITY_ID + "\",\n"
      + " \"code\":\"facilityCode\",\n"
      + " \"name\":\"facilityNameA\",\n"
      + " \"active\":true,\n"
      + " \"enabled\":true,\n"
      + " \"operator\": {\n"
      + "  \"id\": \"9456c3e9-c4a6-4a28-9e08-47ceb16a4121\",\n"
      + "  \"code\": \"moh\",\n"
      + "  \"name\": \"Ministry of Health\",\n"
      + "  \"displayOrder\": 1\n"
      + " },\n"
      + " \"type\": {\n"
      + "  \"id\": \"ac1d268b-ce10-455f-bf87-9c667da8f060\",\n"
      + "  \"code\": \"health_center\",\n"
      + "  \"name\": \"Health Center\",\n"
      + "  \"active\": \"true\",\n"
      + "  \"displayOrder\": 1\n"
      + " },\n"
      + " \"geographicZone\": {\n"
      + "  \"id\": \"4e471242-da63-436c-8157-ade3e615c848\",\n"
      + "  \"code\": \"Mal\",\n"
      + "  \"name\": \"Malawi\",\n"
      + "  \"level\": {\n"
      + "    \"id\": \"6b78e6c6-292e-4733-bb9c-3d802ad61206\",\n"
      + "    \"name\": \"Country\",\n"
      + "    \"code\": \"Country\",\n"
      + "    \"levelNumber\": 1\n"
      + "  }\n"
      + " }\n"
      + "}";
  protected static final String MOCK_SEARCH_SUPPLYING_FACILITY_RESULT = "["
      + MOCK_FIND_FACILITY_RESULT + "]";

  private static final String MOCK_FIND_STOCK_ADJUSTMENT_REASONS_RESULT = "[{"
      + " \"id\":\"62c44f68-9200-1de2-22ea-34b5f98f121a\",\n"
      + " \"programId\":\"5c5a6f68-8658-11e6-ae22-56b6b6499611\",\n"
      + " \"additive\":\"true\",\n"
      + " \"displayOrder\":1\n"
      + "}]";

  private static final String MOCK_FIND_USER_RESULT = "{"
      + "\"id\":\"35316636-6264-6331-2d34-3933322d3462\","
      + "\"username\":\"admin\","
      + "\"firstName\":\"Admin\","
      + "\"lastName\":\"User\","
      + "\"email\":\"example@mail.com\","
      + "\"verified\":false,"
      + "\"active\": true,"
      + "\"homeFacilityId\": \"" + FACILITY_ID + "\","
      + "\"fulfillmentFacilities\": [" + MOCK_FIND_FACILITY_RESULT + "]"
      + "}";

  private static final String MOCK_USER_SEARCH_RESULT = "{"
      + "\"content\": [" + MOCK_FIND_USER_RESULT + "],"
      + "\"last\": true,"
      + "\"totalPages\": 1,"
      + "\"totalElements\": 1,"
      + "\"sort\": null,"
      + "\"first\": true,"
      + "\"numberOfElements\": 1,"
      + "\"size\": 1,"
      + "\"number\": 1"
      + "}";

  private static final String MOCK_FIND_USER_SUPERVISED_PROGRAMS = "[{"
      + " \"id\":\"5c5a6f68-8658-11e6-ae22-56b6b6499611\""
      + "}]";

  private static final String MOCK_FIND_PRODUCT_RESULT = "{"
      + " \"id\":\"cd9e1412-8703-11e6-ae22-56b6b6499611\",\n"
      + " \"productCode\":\"Product Code\",\n"
      + " \"fullProductName\":\"Product Name\",\n"
      + " \"netContent\":10,\n"
      + " \"packRoundingThreshold\":5,\n"
      + " \"roundToZero\":false\n"
      + "}";

  private static final String MOCK_SEARCH_PRODUCTS = "{"
          + "\"content\": [" + MOCK_FIND_PRODUCT_RESULT + "],"
          + "\"last\": true,"
          + "\"totalPages\": 1,"
          + "\"totalElements\": 1,"
          + "\"sort\": null,"
          + "\"first\": true,"
          + "\"numberOfElements\": 1,"
          + "\"size\": 1,"
          + "\"number\": 1"
          + "}";

  private static final String MOCK_FIND_PROCESSING_SCHEDULE = "{"
      + " \"id\":\"c73ad6a4-895c-11e6-ae22-56b6b6499611\","
      + " \"code\":\"Schedule Code\","
      + " \"name\":\"Schedule Name\""
      + "}";

  private static final String MOCK_SEARCH_PROCESSING_SCHEDULE = "["
      + MOCK_FIND_PROCESSING_SCHEDULE
      + "]";

  private static final String MOCK_FIND_PROCESSING_PERIOD = "{"
      + " \"id\":\"4c6b05c2-894b-11e6-ae22-56b6b6499611\","
      + " \"name\":\"Period Name\","
      + " \"description\":\"Period Description\","
      + "\"processingSchedule\":" + MOCK_FIND_PROCESSING_SCHEDULE + ","
      + " \"startDate\":\"2017-01-01\","
      + " \"endDate\":\"2017-01-31\""
      + " }";

  private static final String MOCK_FIND_FACILITY_TYPE = "{"
      + " \"id\":\"7fbef45e-8961-11e6-ae22-56b6b6499611\","
      + " \"code\":\"Facility Type Code\""
      + "}";

  private static final String MOCK_FIND_ORDERABLE_DISPLAY_CATEGORY = "{"
      + " \"id\":\"6d469a06-8962-11e6-ae22-56b6b6499611\""
      + "}";

  private static final String MOCK_FIND_PROGRAM_ORDERABLE = "{"
      + " \"id\":\"047cb32a-8962-11e6-ae22-56b6b6499611\","
      + " \"program\":" + MOCK_FIND_PROGRAM_RESULT + ","
      + " \"product\":" + MOCK_FIND_PRODUCT_RESULT + ","
      + " \"OrderableDisplayCategory\":" + MOCK_FIND_ORDERABLE_DISPLAY_CATEGORY
      + "}";

  private static final String MOCK_SEARCH_SUPPLY_LINE_RESULT = "[{\n"
      + " \"id\":\"99cd664e-871a-11e6-ae22-56b6b6499611\",\n"
      + " \"supervisoryNode\":\"aa66b244-871a-11e6-ae22-56b6b6499611\",\n"
      + " \"program\":\"aa66b58c-871a-11e6-ae22-56b6b6499611\",\n"
      + " \"supplyingFacility\":\"aa66b762-871a-11e6-ae22-56b6b6499611\"\n"
      + "}]";

  private static final String MOCK_SEARCH_FACILITY_TYPE_APPROVED_PRODUCTS = "[{"
      + " \"id\":\"d0d5e0d6-8962-11e6-ae22-56b6b6499611\","
      + " \"facilityType\":" + MOCK_FIND_FACILITY_TYPE + ","
      + " \"programOrderable\":" + MOCK_FIND_PROGRAM_ORDERABLE + ","
      + " \"maxPeriodsOfStock\": 2"
      + "}]";

  private static final String MOCK_SEARCH_PROCESSING_PERIODS = "["
      +
        MOCK_FIND_PROCESSING_PERIOD
      + "]";

  private static final String MOCK_SEARCH_FACILITIES_WITH_SIMILAR_CODE_OR_NAME = "["
      + "{"
      + " \"id\":\"aaf12a5a-8b16-11e6-ae22-56b6b6499611\",\n"
      + " \"code\":\"facilityCode\",\n"
      + " \"name\":\"facilityNameA\",\n"
      + " \"active\":true,\n"
      + " \"enabled\":true\n"
      + "}"
      + "]";

  private static final String MOCK_RIGHT_SEARCH = "["
      + "{"
      + "\"id\":\"00fb0d27-7ea7-4196-adf0-61103058e0e8\",\n"
      + "\"name\":\"rightName\"\n"
      + "}"
      + "]";

  private static final String MOCK_HAS_RIGHT = "{ \"result\":true }";
  private static final String ORDER = "order";

  public static final String MOCK_EMPTY_PAGE = "{\n"
      + "  \"content\": [],\n"
      + "  \"pageable\": {\n"
      + "    \"sort\": [\n"
      + "    ],\n"
      + "    \"pageNumber\": 0,\n"
      + "    \"pageSize\": 0,\n"
      + "    \"offset\": 0,\n"
      + "    \"unpaged\": false,\n"
      + "    \"paged\": true\n"
      + "  },\n"
      + "  \"totalElements\": 0,\n"
      + "  \"last\": true,\n"
      + "  \"totalPages\": 0,\n"
      + "  \"first\": true,\n"
      + "  \"sort\": [\n"
      + "  ],\n"
      + "  \"numberOfElements\": 0,\n"
      + "  \"size\": 0,\n"
      + "  \"number\": 0,\n"
      + "  \"empty\": true\n"
      + "}";

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  @LocalServerPort
  private int randomPort;

  protected RestAssuredClient restAssured;

  protected static final String BASE_URL = System.getenv("BASE_URL");

  @MockBean
  protected OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @MockBean
  protected FileTemplateRepository fileTemplateRepository;

  @MockBean
  protected FileColumnRepository fileColumnRepository;

  @Autowired
  ExporterBuilder exporter;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Constructor for test.
   */
  public BaseWebIntegrationTest() {
    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_CHECK_RESULT)));

    // This mocks the auth token request response
    wireMockRule.stubFor(post(urlPathEqualTo("/api/oauth/token?grant_type=client_credentials"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_TOKEN_REQUEST_RESPONSE)));

    // This mocks the call to auth to post to an auth user.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/users"))
        .willReturn(aResponse()
            .withStatus(200)));

    // This mocks the call to notification to post a notification.
    wireMockRule.stubFor(post(urlPathEqualTo("/api/notification"))
        .willReturn(aResponse()
            .withStatus(200)));

    // This mocks searching for users
    wireMockRule.stubFor(post(urlMatching("/api/users/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_USER_SEARCH_RESULT)));

    // This mocks for find one user
    wireMockRule.stubFor(get(urlMatching(REFERENCEDATA_API_USERS + UUID_REGEX + ".*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_USER_RESULT)));

    // This mocks the call to retrieve programs supervised by the user
    wireMockRule.stubFor(get(urlMatching(REFERENCEDATA_API_USERS + UUID_REGEX + "/programs.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_USER_SUPERVISED_PROGRAMS)));

    // This mocks the call to retrieve fulfillment facilities of the user
    wireMockRule.stubFor(
        get(urlMatching(REFERENCEDATA_API_USERS + UUID_REGEX + "/fulfillmentFacilities.*"))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody("[" + MOCK_FIND_FACILITY_RESULT + "]")));

    // This mocks for find stock adjustment reasons for program
    wireMockRule.stubFor(get(urlMatching("/api/stockAdjustmentReasons/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_STOCK_ADJUSTMENT_REASONS_RESULT)));

    // This mocks for find one facility
    wireMockRule.stubFor(get(urlMatching("/api/facilities/" + UUID_REGEX + ".*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_FACILITY_RESULT)));

    // This mocks searching for orderables
    wireMockRule.stubFor(get(urlMatching("/api/orderables.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_PRODUCTS)));

    // This mocks for find one orderable
    wireMockRule.stubFor(get(urlMatching("/api/orderables/" + UUID_REGEX + ".*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_PRODUCT_RESULT)));

    // This mocks searching for supplying facilities
    wireMockRule.stubFor(get(urlMatching("/api/facilities/supplying.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_SUPPLYING_FACILITY_RESULT)));

    // This mocks searching for processingSchedules
    wireMockRule.stubFor(get(urlMatching("/api/processingSchedules/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_PROCESSING_SCHEDULE)));

    // This mocks retrieving single processing schedule
    wireMockRule.stubFor(get(urlMatching("/api/processingSchedules/" + UUID_REGEX + ".*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_FIND_PROCESSING_SCHEDULE)));

    // This mocks searching for supplyLines
    wireMockRule.stubFor(get(urlMatching("/api/supplyLines/searchByUUID.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_SUPPLY_LINE_RESULT)));

    // This mocks searching for facilityTypeApprovedProducts
    wireMockRule.stubFor(get(urlMatching("/api/facilityTypeApprovedProducts/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_FACILITY_TYPE_APPROVED_PRODUCTS)));

    // This mocks searching for processingPeriods
    wireMockRule.stubFor(get(urlMatching("/api/processingPeriods/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_PROCESSING_PERIODS)));

    // This mocks searching for processingPeriods by UUID and date
    wireMockRule.stubFor(get(urlMatching("/api/processingPeriods/searchByUUIDAndDate.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_PROCESSING_PERIODS)));

    // This mocks searching facilities with similar facilityCode or facilityName
    wireMockRule.stubFor(get(urlMatching("/api/facilities/search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_SEARCH_FACILITIES_WITH_SIMILAR_CODE_OR_NAME)));

    // This mocks for checking if a user has a right
    wireMockRule.stubFor(get(urlMatching(REFERENCEDATA_API_USERS + UUID_REGEX + "/hasRight.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_HAS_RIGHT))
    );

    // This mocks searching for right by name
    wireMockRule.stubFor(get(urlMatching(REFERENCEDATA_API_RIGHTS + "search.*"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON)
            .withBody(MOCK_RIGHT_SEARCH))
    );
  }

  /**
   * Initialize the REST Assured client. Done here and not in the constructor, so that randomPort is
   * available.
   */
  @PostConstruct
  public void init() {
    RestAssured.baseURI = BASE_URL;
    RestAssured.port = randomPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((clazz, charset) -> objectMapper)
    );
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

    restAssured = ramlDefinition.createRestAssured();
  }

  @Before
  public void setUpBootstrapData() {
    // data from bootstrap.sql
    FileTemplate template = addOrderFileTemplate();
    FileColumn column1 = addOrderFileColumn(
        "33b2d2e9-3167-46b0-95d4-1295be9afc21", true, "fulfillment.header.order.number",
        "Order number", true, 1, null, ORDER, "orderCode", null, null, template
    );
    FileColumn column2 = addOrderFileColumn(
        "6b8d331b-a0dd-4a1f-aafb-40e6a72ab9f6", true, "fulfillment.header.facility.code",
        "Facility code", true, 2, null, ORDER, "facilityId", "Facility", "code", template
    );
    FileColumn column3 = addOrderFileColumn(
        "752cda76-0db5-4b6e-bb79-0f531ab78e2e", true, "fulfillment.header.product.code",
        "Product code", true, 3, null, "lineItemOrderable", "id", "Orderable",
        "productCode", template
    );
    FileColumn column4 = addOrderFileColumn(
        "9e825396-269d-4873-baa4-89054e2722f5", true, "fulfillment.header.product.name",
        "Product name", true, 4, null, "lineItemOrderable", "id", "Orderable",
        "fullProductName", template
    );
    FileColumn column5 = addOrderFileColumn(
        "cd57f329-f549-4717-882e-ecbf98122c39", true, "fulfillment.header.ordered.quantity",
        "Ordered quantity", true, 5, null, "lineItem", "orderedQuantity", null, null, template
    );
    FileColumn column6 = addOrderFileColumn(
        "d0e1aec7-1556-4dc1-8e21-d80a2d76b678", true, "fulfillment.header.period", "Period", true,
        6, "MM/yy", ORDER, "processingPeriodId", "ProcessingPeriod", "startDate", template
    );
    FileColumn column7 = addOrderFileColumn(
        "dab6eec0-4cb4-4d4c-94b7-820308da73ff", true, "fulfillment.header.order.date", "Order date",
        true, 7, "dd/MM/yy", ORDER, "createdDate", null, null, template
    );

    given(fileColumnRepository.findAll()).willReturn(Lists.newArrayList(
        column1, column2, column3, column4, column5, column6, column7
    ));

    addOrderNumberConfiguration();
  }

  private OrderNumberConfiguration addOrderNumberConfiguration() {
    OrderNumberConfiguration configuration = new OrderNumberConfiguration(
        "ORDER-", true, false, true
    );
    configuration.setId(UUID.fromString("70543032-b131-4219-b44d-7781d29db330"));

    given(orderNumberConfigurationRepository.findById(configuration.getId()))
        .willReturn(Optional.of(configuration));
    given(orderNumberConfigurationRepository.findAll())
        .willReturn(Lists.newArrayList(configuration));

    return configuration;
  }

  private FileTemplate addOrderFileTemplate() {
    FileTemplate template = new FileTemplate();
    template.setId(UUID.fromString("457ed5b0-80d7-4cb6-af54-e3f6138c8128"));
    template.setFilePrefix("O");
    template.setHeaderInFile(true);
    template.setTemplateType(TemplateType.ORDER);
    template.setFileColumns(Lists.newArrayList());

    given(fileTemplateRepository.findById(template.getId())).willReturn(Optional.of(template));
    given(fileTemplateRepository.findAll()).willReturn(Lists.newArrayList(template));
    given(fileTemplateRepository.findFirstByTemplateType(TemplateType.ORDER))
        .willReturn(template);

    return template;
  }

  private FileColumn addOrderFileColumn(String id, boolean openLmisField,
                                           String dataFieldLabel, String columnLabel,
                                           boolean include, int position, String format,
                                           String nested, String keyPath, String related,
                                           String relatedKeyPath, FileTemplate template) {
    FileColumn column = new FileColumn();
    column.setId(UUID.fromString(id));
    column.setOpenLmisField(openLmisField);
    column.setDataFieldLabel(dataFieldLabel);
    column.setColumnLabel(columnLabel);
    column.setInclude(include);
    column.setPosition(position);
    column.setFormat(format);
    column.setNested(nested);
    column.setKeyPath(keyPath);
    column.setRelated(related);
    column.setRelatedKeyPath(relatedKeyPath);
    column.setFileTemplate(template);

    template.getFileColumns().add(column);

    given(fileColumnRepository.findById(column.getId())).willReturn(Optional.of(column));

    return column;
  }

  protected String getTokenHeader() {
    return "Bearer 418c89c5-7f21-4cd1-a63a-38c47892b0fe";
  }

  protected UUID getNonMatchingUuid(UUID match) {
    UUID uuid;

    do {
      uuid = UUID.randomUUID();
    } while (uuid.equals(match));

    return uuid;
  }

  public UUID getSharedFacilityId() {
    return UUID.fromString("aaf12a5a-8b16-11e6-ae22-56b6b6499611");
  }

  void denyUserAllRights() {
    wireMockRule.stubFor(
        get(urlMatching(REFERENCEDATA_API_USERS + UUID_REGEX + "/hasRight.*"))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody("{ \"result\":\"false\" }"))
    );
  }

  void denyUserAllRightsForWarehouse(UUID warehouse) {
    String url = String.format(
        "%s%s/hasRight.*warehouseId=%s.*",
        REFERENCEDATA_API_USERS, UUID_REGEX, warehouse
    );
    wireMockRule.stubFor(get(urlMatching(url)).willReturn(aResponse()
        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
        .withBody("{ \"result\":\"false\" }"))
    );
  }

  <T> List<T> getPageContent(Page page, Class<T> type) {
    List content = page.getContent();

    if (isEmpty(content)) {
      // nothing to do
      return Collections.emptyList();
    }

    if (type.isInstance(content.get(0))) {
      // content contains instances of the given type
      return Collections.checkedList(content, type);
    }

    if (content.get(0) instanceof Map) {
      // jackson does not convert json to correct type
      // instead it use default operation and objects are
      // represented by Map
      
      // We have to do manually convert map to the given type =(
      ObjectMapper mapper = new ObjectMapper();
      mapper.findAndRegisterModules();

      TypeFactory factory = mapper.getTypeFactory();
      CollectionLikeType collectionType = factory.constructCollectionLikeType(List.class, type);

      return mapper.convertValue(content, collectionType);
    }

    throw new IllegalStateException("the page content contains unsupported type");
  }

  static class SaveAnswer<T extends BaseEntity> implements Answer<T> {

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
      T obj = (T) invocation.getArguments()[0];

      if (null == obj) {
        return null;
      }

      if (null == obj.getId()) {
        obj.setId(UUID.randomUUID());
      }

      extraSteps(obj);

      return obj;
    }

    void extraSteps(T obj) {
      // should be overridden if extra steps are required.
    }

  }

}
