package org.openlmis.fulfillment.service;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.repository.FacilityFtpSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OrderFileStorage implements OrderStorage {

  @Autowired
  private OrderCsvHelper csvHelper;

  @Autowired
  private OrderFileTemplateService orderFileTemplateService;

  @Autowired
  private FacilityFtpSettingRepository facilityFtpSettingRepository;

  @Override
  public void store(Order order) throws OrderStorageException {
    // retrieve order file template
    OrderFileTemplate template = orderFileTemplateService.getOrderFileTemplate();
    String fileName = template.getFilePrefix() + order.getOrderCode() + ".csv";

    FacilityFtpSetting setting = facilityFtpSettingRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    Path path;

    try {
      Files.createDirectories(Paths.get(setting.getLocalDirectory()));
      path = Paths.get(setting.getLocalDirectory(), fileName);
    } catch (IOException exp) {
      throw new OrderStorageException("I/O while creating the local directory", exp);
    }

    try (Writer writer = Files.newBufferedWriter(path)) {
      // 1. generate CSV file using order file template
      // 2. save generated CSV file in local directory
      csvHelper.writeCsvFile(order, template, writer);
    } catch (IOException exp) {
      throw new OrderStorageException("I/O while creating the order CSV file", exp);
    }
  }

  @Override
  public void delete(Order order) throws OrderStorageException {
    try {
      Files.deleteIfExists(getOrderAsPath(order));
    } catch (IOException exp) {
      throw new OrderStorageException("I/O while deleting the order CSV file", exp);
    }
  }

  @Override
  public Path getOrderAsPath(Order order) {
    OrderFileTemplate template = orderFileTemplateService.getOrderFileTemplate();
    FacilityFtpSetting setting = facilityFtpSettingRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    String fileName = template.getFilePrefix() + order.getOrderCode() + ".csv";

    return Paths.get(setting.getLocalDirectory(), fileName);
  }

}
