package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

@Component
public class OrderFileStorage implements OrderStorage {
  private static final String LOCAL_DIR = "/var/lib/openlmis/fulfillment/orders";

  @Autowired
  private OrderCsvHelper csvHelper;

  @Autowired
  private OrderFileTemplateService orderFileTemplateService;

  @Autowired
  private OrderFtpSender orderFtpSender;

  @PostConstruct
  public void init() throws IOException {
    Files.createDirectories(Paths.get(LOCAL_DIR));
  }

  @Override
  public void store(Order order) throws OrderStorageException {
    // retrieve order file template
    OrderFileTemplate template = orderFileTemplateService.getOrderFileTemplate();
    String fileName = template.getFilePrefix() + order.getOrderCode() + ".csv";

    Path path = Paths.get(LOCAL_DIR, fileName);

    try (Writer writer = Files.newBufferedWriter(path)) {
      // 1. generate CSV file using order file template
      // 2. save generated CSV file in local directory
      csvHelper.writeCsvFile(order, template, writer);
    } catch (IOException exp) {
      throw new OrderStorageException("I/O while creating the order CSV file", exp);
    }

    try {
      // send CSV file over FTP
      orderFtpSender.send(order, path);
      // if everything is OK delete local file
      Files.deleteIfExists(path);
    } catch (IOException exp) {
      throw new OrderStorageException("I/O while deleting the order CSV file", exp);
    }
  }

}
