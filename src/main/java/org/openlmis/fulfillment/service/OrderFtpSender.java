package org.openlmis.fulfillment.service;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;

@Component
public class OrderFtpSender implements OrderSender {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFtpSender.class);

  private static final String CAMEL_FTP_PATTERN =
      "{0}://{1}@{2}:{3}/{4}?password={5}&passiveMode={6}";

  @Autowired
  private ProducerTemplate producerTemplate;

  @Autowired
  private OrderStorage orderStorage;

  @Autowired
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Override
  public boolean send(Order order) throws OrderSenderException {
    Path path = orderStorage.getOrderAsPath(order);
    FacilityFtpSetting setting = facilityFtpSettingRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    try {
      String endpointUri = createEndpointUri(setting);
      File file = path.toFile();
      producerTemplate.sendBodyAndHeader(endpointUri, file, Exchange.FILE_NAME, file.getName());
    } catch (Exception exp) {
      LOGGER.error(
          "Can't transfer CSV file {} related with order {} to the FTP server",
          path, order.getId(), exp
      );

      return false;
    }

    return true;
  }

  private String createEndpointUri(FacilityFtpSetting setting) {
    return MessageFormat.format(CAMEL_FTP_PATTERN,
        setting.getProtocol(),
        setting.getUsername(),
        setting.getServerHost(),
        setting.getServerPort(),
        setting.getRemoteDirectory(),
        setting.getPassword(),
        setting.isPassiveMode());
  }
}
