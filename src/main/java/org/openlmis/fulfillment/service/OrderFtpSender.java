package org.openlmis.fulfillment.service;

import static org.springframework.integration.support.MessageBuilder.withPayload;

import org.openlmis.fulfillment.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;

@Component
public class OrderFtpSender implements OrderSender<Path> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFtpSender.class);

  @Autowired
  @Qualifier("toFtpChannel")
  private MessageChannel toFtpChannel;

  @Override
  public void send(Order order, Path arg) {
    try {
      Message<File> message = withPayload(arg.toFile()).build();
      toFtpChannel.send(message);
    } catch (Exception exp) {
      LOGGER.error("Can't transfer CSV file for order {} to the FTP server", order.getId(), exp);
    }
  }

}
