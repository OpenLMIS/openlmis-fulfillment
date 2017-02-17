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

package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Transactional
@SpringBootTest
@DirtiesContext
@RunWith(SpringRunner.class)
public class ConfigurationSettingRepositoryIntegrationTest {
  private AtomicInteger instanceNumber = new AtomicInteger(0);

  @Autowired
  private ConfigurationSettingRepository repository;

  @Test
  public void testUpdate() {
    // given
    ConfigurationSetting saved = createConfigurationSetting();

    // when
    saved.setValue("my_secret_value");

    ConfigurationSetting newValue = repository.save(saved);

    // then
    assertThat(newValue, is(notNullValue()));
    assertThat(repository.exists(newValue.getKey()), is(true));
    assertThat(newValue.getKey(), is(saved.getKey()));
    assertThat(newValue.getValue(), is("my_secret_value"));
  }

  @Test
  public void testFindOne() {
    // given
    ConfigurationSetting setting = createConfigurationSetting();

    // when
    ConfigurationSetting found = repository.findOne(setting.getKey());

    // then
    assertThat(found.getKey(), is(equalTo(setting.getKey())));
    assertThat(found.getValue(), is(equalTo(setting.getValue())));
  }

  @Test
  public void testFindAll() {
    // given
    IntStream.range(0, 5).forEach(idx -> createConfigurationSetting());

    // when
    List<ConfigurationSetting> all = Lists.newArrayList(repository.findAll());

    // then
    assertThat(all, hasSize(5));
  }

  private ConfigurationSetting createConfigurationSetting() {
    ConfigurationSetting saved = repository.save(generateInstance());

    assertThat(saved, is(notNullValue()));
    assertThat(repository.exists(saved.getKey()), is(true));
    return saved;
  }

  private ConfigurationSetting generateInstance() {
    ConfigurationSetting setting = new ConfigurationSetting();
    setting.setKey("key" + getNextInstanceNumber());
    setting.setValue(RandomStringUtils.random(25));

    return setting;
  }

  private int getNextInstanceNumber() {
    return this.instanceNumber.incrementAndGet();
  }

}
