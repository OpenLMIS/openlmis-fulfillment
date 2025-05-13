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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.Lists;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.web.util.OrderNumberConfigurationDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class OrderNumberConfigurationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderNumberConfigurations";

  @MockBean
  private OrderRepository orderRepository;

  private UUID facility = UUID.fromString("1d5bdd9c-8702-11e6-ae22-56b6b6499611");

  private OrderNumberConfiguration firstOrderNumberConfiguration;
  private OrderNumberConfiguration secondOrderNumberConfiguration;

  @Before
  public void setUp() {
    this.setUpBootstrapData();

    ProgramDto programDto = new ProgramDto();
    programDto.setId(UUID.fromString("35316636-6264-6331-2d34-3933322d3462"));
    programDto.setCode("code");

    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setExternalId(UUID.randomUUID());
    order.setEmergency(true);
    order.setFacilityId(facility);
    order.setProcessingPeriodId(UUID.fromString("a510d22f-f370-46c7-88e2-981573c427f5"));
    order.setCreatedById(UUID.randomUUID());
    order.setProgramId(programDto.getId());
    order.setRequestingFacilityId(facility);
    order.setReceivingFacilityId(facility);
    order.setSupplyingFacilityId(facility);
    order.setOrderCode("order_code");
    order.setStatus(OrderStatus.ORDERED);
    order.setQuotedCost(BigDecimal.ONE);

    firstOrderNumberConfiguration =  new OrderNumberConfiguration("prefix", true, true, true);
    secondOrderNumberConfiguration =  new OrderNumberConfiguration("prefix", false, false, false);


    given(orderRepository.findById(order.getId())).willReturn(Optional.of(order));
    given(orderRepository.existsById(order.getId())).willReturn(true);

    given(orderNumberConfigurationRepository.save(any(OrderNumberConfiguration.class)))
        .willAnswer(new SaveAnswer<OrderNumberConfiguration>());


  }

  @Test
  public void shouldGetOrderNumberConfiguration() {
    given(orderNumberConfigurationRepository.findAll()).willReturn(Lists.newArrayList(
        firstOrderNumberConfiguration, secondOrderNumberConfiguration));

    given(orderNumberConfigurationRepository.existsById(firstOrderNumberConfiguration.getId()))
        .willReturn(true);

    OrderNumberConfigurationDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract().as(OrderNumberConfigurationDto.class);

    assertTrue(orderNumberConfigurationRepository.existsById(response.getId()));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturnNotFoundWHenThereIsNoOrderNumberConfiguration() {
    given(orderNumberConfigurationRepository.findAll()).willReturn(new ArrayList());

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldUpdateOrderNumberConfiguration() {
    OrderNumberConfiguration orderNumberConfiguration = generate("prefix", true, true, true);
    OrderNumberConfigurationDto orderNumberConfigurationDto = OrderNumberConfigurationDto
        .newInstance(orderNumberConfiguration);

    OrderNumberConfigurationDto response = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfigurationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(OrderNumberConfigurationDto.class);
    orderNumberConfigurationDto.setId(response.getId());

    assertEquals(response, orderNumberConfigurationDto);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn400WhenSavingConfigurationWithNotAlphanumericPrefix() {
    final String notAlphanumericString = "..dsa2,";

    OrderNumberConfiguration orderNumberConfiguration = generate(notAlphanumericString, true,
        false, false);
    OrderNumberConfigurationDto orderNumberConfigurationDto = OrderNumberConfigurationDto
        .newInstance(orderNumberConfiguration);

    postForOrderNumberConfiguration(orderNumberConfigurationDto, 400);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn400WhenSavingConfigurationWithPrefixLongerThan8Characters() {
    final String tooLongPrefix = "123456789";

    OrderNumberConfiguration orderNumberConfiguration = generate(tooLongPrefix, true, false, false);
    OrderNumberConfigurationDto orderNumberConfigurationDto = OrderNumberConfigurationDto
        .newInstance(orderNumberConfiguration);

    postForOrderNumberConfiguration(orderNumberConfigurationDto, 400);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private OrderNumberConfiguration generate(String prefix, Boolean includeOrderNumberPrefix,
                                            Boolean includeProgramCode, Boolean includeTypeSuffix) {
    OrderNumberConfiguration orderNumberConfiguration = new OrderNumberConfiguration();
    orderNumberConfiguration.setId(UUID.randomUUID());
    orderNumberConfiguration.setOrderNumberPrefix(prefix);
    orderNumberConfiguration.setIncludeOrderNumberPrefix(includeOrderNumberPrefix);
    orderNumberConfiguration.setIncludeProgramCode(includeProgramCode);
    orderNumberConfiguration.setIncludeTypeSuffix(includeTypeSuffix);

    return orderNumberConfiguration;
  }

  private void postForOrderNumberConfiguration(OrderNumberConfigurationDto orderNumberConfiguration,
                                               Integer code) {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfiguration)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(code);
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToViewOrderNumberConfiguration() {
    denyUserAllRights();

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateOrderNumberConfiguration() {
    denyUserAllRights();
    OrderNumberConfiguration orderNumberConfiguration = generate("stuff", true, true, true);
    OrderNumberConfigurationDto orderNumberConfigurationDto = OrderNumberConfigurationDto
        .newInstance(orderNumberConfiguration);

    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfigurationDto)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

}
