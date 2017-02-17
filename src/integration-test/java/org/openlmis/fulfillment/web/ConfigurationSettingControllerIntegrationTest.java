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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.openlmis.fulfillment.repository.ConfigurationSettingRepository;
import org.openlmis.fulfillment.web.util.ConfigurationSettingDto;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import guru.nidi.ramltester.junit.RamlMatchers;

import java.util.Collections;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.UnusedPrivateField"})
public class ConfigurationSettingControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String RESOURCE_URL = "/api/configurationSettings";
  private static final String ACCESS_TOKEN = "access_token";

  private static final String KEY = "key";
  private static final String VALUE = "value";

  @MockBean
  private ConfigurationSettingRepository configurationSettingRepository;

  @Test
  public void shouldRetrieveAllSettings() {
    ConfigurationSetting setting = new ConfigurationSetting();
    setting.setKey(KEY);
    setting.setValue(VALUE);

    given(configurationSettingRepository.findAll())
        .willReturn(Collections.singletonList(setting));

    ConfigurationSettingDto[] array = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .when()
        .get(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .body()
        .as(ConfigurationSettingDto[].class);

    assertThat(array.length, is(1));
    assertThat(array[0].getKey(), is(KEY));
    assertThat(array[0].getValue(), is(VALUE));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.responseChecks());
  }

  @Test
  public void shouldUpdateSetting() {
    ConfigurationSetting setting = new ConfigurationSetting();
    setting.setKey(KEY);
    setting.setValue(VALUE);

    given(configurationSettingRepository.findOne(KEY))
        .willReturn(setting);
    given(configurationSettingRepository.save(any(ConfigurationSetting.class)))
        .willAnswer(invocation -> invocation.getArguments()[0]);

    ConfigurationSettingDto response = restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(new ConfigurationSettingDto(KEY, VALUE + "2"))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(200)
        .extract()
        .body()
        .as(ConfigurationSettingDto.class);

    assertThat(response.getKey(), is(KEY));
    assertThat(response.getValue(), is(VALUE + "2"));
    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

  @Test
  public void shouldReturn403WhenUserHasNoRightsToViewSettings() {
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

  @Test
  public void shouldReturn403WhenUserHasNoRightsToUpdateSettings() {
    denyUserAllRights();

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(new ConfigurationSettingDto(KEY, VALUE))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

  }

  @Test
  public void shouldReturn404IfSettingNotExist() {
    given(configurationSettingRepository.findOne(KEY)).willReturn(null);

    restAssured.given()
        .queryParam(ACCESS_TOKEN, getToken())
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .body(new ConfigurationSettingDto(KEY, VALUE))
        .when()
        .put(RESOURCE_URL)
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }

}
