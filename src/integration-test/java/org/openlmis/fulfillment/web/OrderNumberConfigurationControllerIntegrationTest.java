package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.ACCESS_TOKEN;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.referencedata.model.ProgramDto;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.UUID;

@Ignore
public class OrderNumberConfigurationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/orderNumberConfigurations";

  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private OrderRepository orderRepository;

  private Order order;
  private ProgramDto programDto;
  private UUID facility = UUID.fromString("1d5bdd9c-8702-11e6-ae22-56b6b6499611");

  @Before
  public void setUp() {

    programDto = new ProgramDto();
    programDto.setId(UUID.fromString("35316636-6264-6331-2d34-3933322d3462"));
    programDto.setCode("code");

    order = new Order();
    order.setEmergency(true);
    order.setProgramId(programDto.getId());
    order.setFacilityId(facility);
    order.setProcessingPeriodId(UUID.fromString("a510d22f-f370-46c7-88e2-981573c427f5"));
    order = orderRepository.save(order);
  }

  @Test
  public void shouldUpdateOrderNumberConfiguration() {
    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration("prefix", true, true, true);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfiguration)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .as(OrderNumberConfiguration.class);

    assertEquals(1, orderNumberConfigurationRepository.count());
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn400WhenSavingConfigurationWithNotAlphanumericPrefix() {
    final String notAlphanumericString = "..dsa2,";

    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration(notAlphanumericString, true, false, false);

    postForOrderNumberConfiguration(orderNumberConfiguration, 400);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn400WhenSavingConfigurationWithPrefixLongerThan8Characters() {
    final String tooLongPrefix = "123456789";

    OrderNumberConfiguration orderNumberConfiguration =
        new OrderNumberConfiguration(tooLongPrefix, true, false, false);

    postForOrderNumberConfiguration(orderNumberConfiguration, 400);
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  private void postForOrderNumberConfiguration(OrderNumberConfiguration orderNumberConfiguration,
                                               Integer code) {
    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(orderNumberConfiguration)
        .when()
        .post(RESOURCE_URL)
        .then()
        .statusCode(code);
  }

}
