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

package org.openlmis.fulfillment.service;

import static org.apache.commons.beanutils.PropertyUtils.getPropertyDescriptors;
import static org.openlmis.fulfillment.domain.OrderStatus.IN_ROUTE;
import static org.openlmis.fulfillment.domain.OrderStatus.READY_TO_PACK;
import static org.openlmis.fulfillment.domain.OrderStatus.TRANSFER_FAILED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_IO;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_JASPER;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_IN_USE;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_NOREPLY;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_ORDER_CREATION_BODY;
import static org.openlmis.fulfillment.util.ConfigurationSettingKeys.FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import org.openlmis.util.NotificationRequest;
import org.openlmis.fulfillment.domain.FtpTransferProperties;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.domain.TransferProperties;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.TransferPropertiesRepository;
import org.openlmis.fulfillment.service.notification.NotificationService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.web.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class OrderService {

  static final String[] DEFAULT_COLUMNS = {"facilityCode", "createdDate", "orderNum",
      "productName", "productCode", "orderedQuantity", "filledQuantity"};

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private TransferPropertiesRepository transferPropertiesRepository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  @Autowired
  private OrderStorage orderStorage;

  @Autowired
  private OrderSender orderSender;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  /**
   * Finds orders matching all of provided parameters.
   *
   * @param params provided parameters.
   * @return ist of Orders with matched parameters.
   */
  public List<Order> searchOrders(OrderSearchParams params) {
    return orderRepository.searchOrders(
        params.getSupplyingFacility(), params.getRequestingFacility(), params.getProgram(),
        params.getProcessingPeriod(), params.getStatusAsEnum()
    );
  }

  /**
   * Changes order to CSV formatted file.
   *
   * @param order         Order type object to be transformed into CSV
   * @param chosenColumns String array containing names of columns to be taken from order
   */
  public void orderToCsv(Order order, String[] chosenColumns,
                         Writer writer) {
    if (null == order) {
      return;
    }

    List<Map<String, Object>> rows = orderToRows(order);

    if (rows.isEmpty()) {
      return;
    }

    try (ICsvMapWriter mapWriter = new CsvMapWriter(writer, STANDARD_PREFERENCE)) {
      mapWriter.writeHeader(chosenColumns);

      for (Map<String, Object> row : rows) {
        mapWriter.write(row, chosenColumns);
      }
    } catch (IOException ex) {
      throw new OrderCsvWriteException(ex, ERROR_IO, ex.getMessage());
    }
  }

  /**
   * Changes order to PDF formatted file given at OutputStream.
   *
   * @param order         Order type object to be transformed into CSV
   * @param chosenColumns String array containing names of columns to be taken from order
   * @param out           OutputStream to which the pdf file content will be written
   */
  public void orderToPdf(Order order, String[] chosenColumns, OutputStream out) {
    if (order != null) {
      List<Map<String, Object>> rows = orderToRows(order);
      try {
        writePdf(rows, chosenColumns, out);
      } catch (JRException ex) {
        throw new OrderPdfWriteException(ex, ERROR_JASPER);
      } catch (IOException ex) {
        throw new OrderPdfWriteException(ex, ERROR_IO, ex.getMessage());
      }
    }
  }

  /**
   * Safe delete of an order. If the order is linked to an existing proof of delivery, a
   * {@link ValidationException} signals that it cannot be removed.
   *
   * @param order the order to remove
   */
  public void delete(Order order) {
    if (null != proofOfDeliveryRepository.findByOrderId(order.getId())) {
      throw new ValidationException(ERROR_ORDER_IN_USE, order.getId().toString());
    } else {
      orderRepository.delete(order);
    }
  }

  private void writePdf(List<Map<String, Object>> data, String[] chosenColumns,
                        OutputStream out) throws JRException, IOException {
    String filePath = "jasperTemplates/ordersJasperTemplate.jrxml";
    ClassLoader classLoader = getClass().getClassLoader();

    try (InputStream fis = classLoader.getResourceAsStream(filePath)) {
      JasperReport pdfTemplate = JasperCompileManager.compileReport(fis);
      Object[] params = new Object[data.size()];

      for (int index = 0; index < data.size(); ++index) {
        Map<String, Object> dataRow = data.get(index);

        params[index] = Stream.of(chosenColumns)
            .collect(Collectors.toMap(column -> column, dataRow::get));
      }

      JRMapArrayDataSource dataSource = new JRMapArrayDataSource(params);
      JasperPrint jasperPrint = JasperFillManager.fillReport(
          pdfTemplate, new HashMap<>(), dataSource
      );

      JRPdfExporter exporter = new JRPdfExporter();
      exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
      exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
      exporter.exportReport();
    }
  }

  private List<Map<String, Object>> orderToRows(Order order) {
    List<Map<String, Object>> rows = new ArrayList<>();

    List<OrderLineItem> orderLineItems = order.getOrderLineItems();
    String orderNum = order.getOrderCode();

    FacilityDto requestingFacility = facilityReferenceDataService.findOne(
        order.getRequestingFacilityId());
    String facilityCode = requestingFacility.getCode();
    ZonedDateTime createdDate = order.getCreatedDate();

    for (OrderLineItem orderLineItem : orderLineItems) {
      Map<String, Object> row = new HashMap<>();

      OrderableDto product = orderableReferenceDataService
          .findOne(orderLineItem.getOrderableId());

      row.put(DEFAULT_COLUMNS[0], facilityCode);
      row.put(DEFAULT_COLUMNS[1], createdDate);
      row.put(DEFAULT_COLUMNS[2], orderNum);
      row.put(DEFAULT_COLUMNS[3], product.getName());
      row.put(DEFAULT_COLUMNS[4], product.getProductCode());
      row.put(DEFAULT_COLUMNS[5], orderLineItem.getOrderedQuantity());
      row.put(DEFAULT_COLUMNS[6], orderLineItem.getFilledQuantity());

      //products which have a final approved quantity of zero are omitted
      if (orderLineItem.getOrderedQuantity() > 0) {
        rows.add(row);
      }
    }
    return rows;
  }

  /**
   * Saves a new instance of order. The method also stores the order in local directory and try
   * to send (if there are FTP transfer properties) to an FTP server. Also, the status field in
   * the order will be updated.
   *
   * @param order instance
   * @return passed instance after save.
   */
  public Order save(Order order) {
    setOrderStatus(order);

    // save order
    Order saved = orderRepository.save(order);

    orderStorage.store(saved);

    TransferProperties properties = transferPropertiesRepository
        .findFirstByFacilityId(order.getSupplyingFacilityId());

    if (properties instanceof FtpTransferProperties) {
      boolean success = orderSender.send(saved);

      if (success) {
        orderStorage.delete(saved);
      } else {
        order.setStatus(TRANSFER_FAILED);
        saved = orderRepository.save(order);
      }
    }

    // Send an email notification to the user that converted the order
    sendNotification(saved, saved.getCreatedById());

    return saved;
  }

  private void sendNotification(Order order, UUID userId) {
    String from = configurationSettingService.getStringValue(FULFILLMENT_EMAIL_NOREPLY);
    String to = userReferenceDataService.findOne(userId).getEmail();
    String subject = configurationSettingService
        .getStringValue(FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT);
    String content = createContent(order);

    notificationService.send(new NotificationRequest(from, to, subject, content));
  }

  private String createContent(Order order) {
    String content = configurationSettingService
        .getStringValue(FULFILLMENT_EMAIL_ORDER_CREATION_BODY);

    try {
      List<PropertyDescriptor> descriptors = Arrays
          .stream(getPropertyDescriptors(order.getClass()))
          .filter(d -> null != d.getReadMethod())
          .collect(Collectors.toList());

      for (PropertyDescriptor descriptor : descriptors) {
        String target = "{" + descriptor.getName() + "}";
        String replacement = String.valueOf(descriptor.getReadMethod().invoke(order));

        content = content.replace(target, replacement);
      }
    } catch (IllegalAccessException | InvocationTargetException exp) {
      throw new IllegalStateException("Can't get access to getter method", exp);
    }
    return content;
  }

  private void setOrderStatus(Order order) {
    // Is the order associated with a supply line?
    if (null != order.getSupplyingFacilityId()) {
      // Is the supplying facility have the FTP configuration?
      TransferProperties properties = transferPropertiesRepository
          .findFirstByFacilityId(order.getSupplyingFacilityId());

      if (null == properties) {
        // Set order status as TRANSFER_FAILED
        order.setStatus(TRANSFER_FAILED);
      } else {
        // Is the export-orders flag enabled on the supply line associated with the order
        // yes -> Set order status as IN_ROUTE
        // no  -> Set order status as READY_TO_PACK
        order.setStatus(properties instanceof FtpTransferProperties ? IN_ROUTE : READY_TO_PACK);
      }
    } else {
      // Set order status as TRANSFER_FAILED
      order.setStatus(TRANSFER_FAILED);
    }
  }

}
