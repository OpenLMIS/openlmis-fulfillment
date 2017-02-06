package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_IO;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OrderFileStorage implements OrderStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFileStorage.class);

  @Autowired
  private OrderCsvHelper csvHelper;

  @Autowired
  private OrderFileTemplateService orderFileTemplateService;

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Override
  public void store(Order order) {
    TransferProperties properties = transferPropertiesRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    if (null == properties) {
      LOGGER.warn(
          "Can't store the order {} because there is no transfer properties",
          order.getId()
      );
      return;
    }

    // retrieve order file template
    OrderFileTemplate template = orderFileTemplateService.getOrderFileTemplate();
    String fileName = template.getFilePrefix() + order.getOrderCode() + ".csv";
    Path path;

    try {
      String dir = properties.getPath();
      Files.createDirectories(Paths.get(dir));
      path = Paths.get(dir, fileName);
    } catch (IOException exp) {
      throw new OrderStorageException(exp, ERROR_IO, exp.getMessage());
    }

    try (Writer writer = Files.newBufferedWriter(path)) {
      // 1. generate CSV file using order file template
      // 2. save generated CSV file in local directory
      csvHelper.writeCsvFile(order, template, writer);
    } catch (IOException exp) {
      throw new OrderStorageException(exp, ERROR_IO, exp.getMessage());
    }
  }

  @Override
  public void delete(Order order) {
    try {
      Files.deleteIfExists(getOrderAsPath(order));
    } catch (IOException exp) {
      throw new OrderStorageException(exp, ERROR_IO, exp.getMessage());
    }
  }

  @Override
  public Path getOrderAsPath(Order order) {
    OrderFileTemplate template = orderFileTemplateService.getOrderFileTemplate();
    TransferProperties properties = transferPropertiesRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    String fileName = template.getFilePrefix() + order.getOrderCode() + ".csv";

    return Paths.get(properties.getPath(), fileName);
  }

}
