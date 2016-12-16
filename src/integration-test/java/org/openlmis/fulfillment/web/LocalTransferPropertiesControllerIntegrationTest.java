package org.openlmis.fulfillment.web;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.openlmis.fulfillment.domain.LocalTransferProperties;

import java.util.UUID;

public class LocalTransferPropertiesControllerIntegrationTest
    extends BaseTransferPropertiesControllerIntegrationTest<LocalTransferProperties> {

  @Override
  LocalTransferProperties generateProperties() {
    LocalTransferProperties local = new LocalTransferProperties();
    local.setId(UUID.randomUUID());
    local.setFacilityId(UUID.randomUUID());
    local.setPath("local/dir");

    return local;
  }

  @Override
  void assertTransferProperties(LocalTransferProperties actual) {
    assertThat(actual.getId(), is(notNullValue()));
    assertThat(actual.getFacilityId(), is(notNullValue()));
    assertThat(actual.getPath(), is("local/dir"));
  }

}
