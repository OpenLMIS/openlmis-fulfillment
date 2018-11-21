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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.domain.TransferType;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.shipment.ShipmentMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

@Order(30)
@Component
public class ShipmentContextRunner implements CommandLineRunner {

  private static final String INCOMING = "/incoming";
  private static final String ERROR = "/error";
  public static final String ARCHIVE = "/archive";

  @Value("${shipment.polling.rate}")
  private String pollingRate;

  @Value("${shipment.shippedById}")
  private String shippedById;

  @Autowired
  TransferPropertiesRepository transferPropertiesService;

  @Autowired
  ShipmentMessageHandler messageHandler;

  @Autowired
  ApplicationContext applicationContext;

  private final Map<UUID, ConfigurableApplicationContext> contexts = new HashMap<>();

  public void run(String... args) throws IOException {
    createFtpChannels();
  }

  /**
   * Returns an initialized context for a transfer property.
   *
   * @param transferPropertyId transfer property id
   */
  public ConfigurableApplicationContext getContext(UUID transferPropertyId) {
    return contexts.get(transferPropertyId);
  }

  private synchronized void createFtpChannel(FtpTransferProperties transferProperties) {
    ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
        new String[]{"/META-INF/shipment-ftp-context.xml"},
        false, applicationContext);
    setEnvironmentForFtpShipmentSource(ctx, transferProperties);
    ctx.refresh();
    this.contexts.put(transferProperties.getId(), ctx);
  }

  /**
   * Initiates ftp channels and contexts for previously configured ftp credentials.
   */
  private void createFtpChannels() {
    List<TransferProperties> propertiesList = transferPropertiesService
        .findByTransferType(TransferType.SHIPMENT);
    for (TransferProperties property : propertiesList) {
      if (FtpTransferProperties.class.equals(property.getClass())) {
        createFtpChannel((FtpTransferProperties) property);
      }
    }
  }

  private void setEnvironmentForFtpShipmentSource(ConfigurableApplicationContext ctx,
      FtpTransferProperties ftp) {

    Properties props = new Properties();
    props.setProperty("host", ftp.getServerHost());
    props.setProperty("user", ftp.getUsername());
    props.setProperty("password", ftp.getPassword());
    props.setProperty("port", ftp.getServerPort().toString());

    props.setProperty("shipment.polling.rate", pollingRate);

    props.setProperty("shipment.shippedById", shippedById);

    props.setProperty("remote.incoming.directory", ftp.getRemoteDirectory() + INCOMING);
    props.setProperty("remote.archive.directory", ftp.getRemoteDirectory() + ARCHIVE);
    props.setProperty("remote.error.directory", ftp.getRemoteDirectory() + ERROR);

    props.setProperty("local.directory", ftp.getLocalDirectory() + INCOMING);

    StandardEnvironment env = new StandardEnvironment();
    env.getPropertySources().addLast(new PropertiesPropertySource("ftp", props));
    ctx.setEnvironment(env);
  }

}