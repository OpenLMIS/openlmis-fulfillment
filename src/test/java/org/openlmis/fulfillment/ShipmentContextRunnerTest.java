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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.fulfillment.domain.FtpProtocol;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.LocalTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.domain.TransferType;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ShipmentContextRunner.class})
public class ShipmentContextRunnerTest {

  private static final String INCOMING = "/incoming";
  private static final String ERROR = "/error";
  private static final String ARCHIVE = "/archive";

  @Mock
  private TransferPropertiesRepository transferPropertiesRepository;

  @InjectMocks
  private ShipmentContextRunner shipmentContextRunner;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(shipmentContextRunner, "pollingRate", "1000");
    ReflectionTestUtils.setField(shipmentContextRunner, "shippedById",
        UUID.randomUUID().toString());
  }

  @Test
  public void shouldNotInitializeCustomContextForLocalTransferType() {
    TransferProperties localTransferProperties = createLocalTransferProperty(TransferType.SHIPMENT);
    when(transferPropertiesRepository.findByTransferType(TransferType.SHIPMENT))
        .thenReturn(asList(localTransferProperties));

    shipmentContextRunner.run();

    Map<UUID, ConfigurableApplicationContext> contexts = Whitebox
        .getInternalState(shipmentContextRunner, "contexts");

    ConfigurableApplicationContext context = contexts.get(localTransferProperties.getId());
    assertNull(context);
  }

  @Test
  public void shouldInitializeCustomContextForFtpTransferType() throws Exception {
    TransferProperties ftpTransferProperties = createFtpTransferProperty(TransferType.SHIPMENT);
    when(transferPropertiesRepository.findByTransferType(TransferType.SHIPMENT))
        .thenReturn(asList(ftpTransferProperties));
    ClassPathXmlApplicationContext mockContext = mock(ClassPathXmlApplicationContext.class);
    doNothing().when(mockContext).refresh();
    StandardEnvironment environment = new StandardEnvironment();
    when(mockContext.getEnvironment()).thenReturn(environment);
    whenNew(ClassPathXmlApplicationContext.class)
        .withArguments(any(String[].class), any(Boolean.class), any(ApplicationContext.class))
        .thenReturn(mockContext);

    shipmentContextRunner.run();

    Map<UUID, ConfigurableApplicationContext> contexts = Whitebox
        .getInternalState(shipmentContextRunner, "contexts");

    ConfigurableApplicationContext context = contexts.get(ftpTransferProperties.getId());
    assertNotNull(context);
    verify(mockContext).refresh();
  }


  @Test
  public void shouldPopulateFtpPropertiesInEnvironment() throws Exception {

    FtpTransferProperties ftpProps = createFtpTransferProperty(TransferType.SHIPMENT);
    when(transferPropertiesRepository.findByTransferType(TransferType.SHIPMENT))
        .thenReturn(asList(ftpProps));
    ClassPathXmlApplicationContext mockContext = mock(ClassPathXmlApplicationContext.class);
    doNothing().when(mockContext).refresh();
    StandardEnvironment environment = new StandardEnvironment();
    when(mockContext.getEnvironment()).thenReturn(environment);
    whenNew(ClassPathXmlApplicationContext.class)
        .withArguments(any(String[].class), any(Boolean.class), any(ApplicationContext.class))
        .thenReturn(mockContext);
    Properties props = mock(Properties.class);
    whenNew(Properties.class).withNoArguments().thenReturn(props);

    shipmentContextRunner.run();

    verify(props).setProperty("host", ftpProps.getServerHost());
    verify(props).setProperty("user", ftpProps.getUsername());
    verify(props).setProperty("password", ftpProps.getPassword());
    verify(props).setProperty("port", ftpProps.getServerPort().toString());
    verify(props)
        .setProperty("remote.incoming.directory", ftpProps.getRemoteDirectory() + INCOMING);
    verify(props).setProperty("remote.error.directory", ftpProps.getRemoteDirectory() + ERROR);
    verify(props).setProperty("remote.archive.directory", ftpProps.getRemoteDirectory() + ARCHIVE);
    verify(props).setProperty("local.directory", ftpProps.getLocalDirectory() + INCOMING);
  }

  private FtpTransferProperties createFtpTransferProperty(TransferType transferType) {
    FtpTransferProperties ftpTransferProperties = new FtpTransferProperties();
    ftpTransferProperties.setId(UUID.randomUUID());
    ftpTransferProperties.setFacilityId(UUID.randomUUID());
    ftpTransferProperties.setTransferType(transferType);
    ftpTransferProperties.setLocalDirectory("/var/lib/openlmis/shipments/");
    ftpTransferProperties.setRemoteDirectory("/shipment/files/csv");
    ftpTransferProperties.setPassiveMode(true);
    ftpTransferProperties.setProtocol(FtpProtocol.FTP);
    ftpTransferProperties.setServerHost("localhost");
    ftpTransferProperties.setUsername("random-user");
    ftpTransferProperties.setPassword("random-password");
    ftpTransferProperties.setServerPort(1000);
    return ftpTransferProperties;
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