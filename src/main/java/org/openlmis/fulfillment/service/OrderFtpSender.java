package org.openlmis.fulfillment.service;

import static org.springframework.integration.support.MessageBuilder.withPayload;

import org.openlmis.fulfillment.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class OrderFtpSender implements OrderSender {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFtpSender.class);

  @Autowired
  @Qualifier("toFtpChannel")
  private MessageChannel toFtpChannel;

  @Autowired
  private OrderFileStorage orderFileStorage;

  @Override
  public boolean send(Order order) throws OrderSenderException {
    Path path = orderFileStorage.get(order);

    try {
      toFtpChannel.send(withPayload(path).build());
    } catch (Exception exp) {
      LOGGER.error("Can't transfer CSV file {} to the FTP server", path, exp);
      return false;
    }

    return true;
  }
}
