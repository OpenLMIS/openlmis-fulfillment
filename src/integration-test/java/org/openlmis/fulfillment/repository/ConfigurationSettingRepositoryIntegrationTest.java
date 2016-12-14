package org.openlmis.fulfillment.repository;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@DirtiesContext
@RunWith(SpringRunner.class)
public class ConfigurationSettingRepositoryIntegrationTest {

  @Autowired
  private ConfigurationSettingRepository repository;

  @Test
  public void testFindOne() {
    ConfigurationSetting setting = repository.save(new ConfigurationSetting("key", "value"));
    ConfigurationSetting found = repository.findOne("key");

    assertThat(found.getKey(), is(equalTo(setting.getKey())));
    assertThat(found.getValue(), is(equalTo(setting.getValue())));
  }

}
