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
  public void testCreate() {
    ConfigurationSetting saved = repository.save(generateInstance());

    assertThat(saved, is(notNullValue()));
    assertThat(repository.exists(saved.getKey()), is(true));
  }

  @Test
  public void testFindOne() {
    ConfigurationSetting setting = repository.save(generateInstance());
    ConfigurationSetting found = repository.findOne(setting.getKey());

    assertThat(found.getKey(), is(equalTo(setting.getKey())));
    assertThat(found.getValue(), is(equalTo(setting.getValue())));
  }

  @Test
  public void testFindAll() {
    IntStream.range(0, 5)
        .mapToObj(idx -> generateInstance())
        .forEach(repository::save);

    List<ConfigurationSetting> all = Lists.newArrayList(repository.findAll());

    assertThat(all, hasSize(5));
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
