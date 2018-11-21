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

package org.openlmis.fulfillment;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.domain.TransferType;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class ShipmentContextRunnerTest {

  @Mock
  TransferPropertiesRepository transferPropertiesRepository;

  @InjectMocks
  ShipmentContextRunner shipmentChannelConfig;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(shipmentChannelConfig, "pollingRate", "1000");
    ReflectionTestUtils.setField(shipmentChannelConfig, "shippedById",
        UUID.randomUUID().toString());
  }

  @Test
  public void shouldNotInitializeCustomContextForLocalTransferType() throws Exception {
    TransferProperties localTransferProperties = createLocalTransferProperty(TransferType.SHIPMENT);
    when(transferPropertiesRepository.findByTransferType(TransferType.SHIPMENT))
        .thenReturn(asList(localTransferProperties));

    shipmentChannelConfig.run();

    ConfigurableApplicationContext context = shipmentChannelConfig
        .getContext(localTransferProperties.getId());
    assertNull(context);
  }


  private TransferProperties createLocalTransferProperty(TransferType transferType) {
    LocalTransferProperties localTransferProperties = new LocalTransferProperties();
    localTransferProperties.setId(UUID.randomUUID());
    localTransferProperties.setFacilityId(UUID.randomUUID());
    localTransferProperties.setTransferType(transferType);
    localTransferProperties.setPath("/var/lib/openlmis/shipments/");
    return localTransferProperties;
  }

}