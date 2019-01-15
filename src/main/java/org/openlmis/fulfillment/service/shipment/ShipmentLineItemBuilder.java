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

package org.openlmis.fulfillment.service.shipment;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.csv.CSVRecord;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.util.FileColumnKeyPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShipmentLineItemBuilder {

  @Autowired
  private OrderableReferenceDataService orderableService;

  private List<FileColumn> extraDataFields;

  private Map<String, OrderableDto> productCodeOrderableMap;

  private Map<UUID, OrderableDto> uuidOrderableMap;

  /**
   * Builds shipment line item objects from parsed CSV data.
   *
   * @param template file template used for parsing
   * @param lines data read from the csv.
   * @return List of ShipmentLineItems
   */
  public List<ShipmentLineItem> build(FileTemplate template, List<CSVRecord> lines) {
    FileColumn orderableColumn = template
        .findColumn(FileColumnKeyPath.ORDERABLE_COLUMN_PATHS).orElse(null);
    FileColumn orderColumn = template
        .findColumn(FileColumnKeyPath.ORDER_COLUMN_PATHS).orElse(null);
    FileColumn quantityShippedColumn = template
        .findColumn(FileColumnKeyPath.QUANTITY_SHIPPED_PATHS).orElse(null);

    if (orderColumn == null || orderableColumn == null || quantityShippedColumn == null) {
      throw new FulfillmentException(
          "Required columns Orderable and/or quantity shipped not found in template.");
    }

    // Initialize and cache variables that would be used repeatedly for each row.
    initializeOrderableMaps();
    initializeExtraFields(template);
    String orderIdentifier = lines.get(0).get(orderColumn.getPosition());

    List<ShipmentLineItem> lineItems = new ArrayList<>();
    for (CSVRecord row : lines) {
      validateOrderIdentifer(orderColumn, orderIdentifier, row);
      UUID orderableId = extractOrderableId(orderableColumn, row);
      String quantityShippedString = row.get(quantityShippedColumn.getPosition());
      Long quantityShipped = Long.parseLong(quantityShippedString);
      Map<String, String> extraData = extractExtraData(row);
      validateOrderableAndQuantity(orderableId, quantityShipped);
      ShipmentLineItem lineItem = new ShipmentLineItem(orderableId, quantityShipped, extraData);
      lineItems.add(lineItem);
    }
    return lineItems;
  }

  private void validateOrderIdentifer(FileColumn orderColumn, String orderIdentifier,
      CSVRecord row) {
    String orderIdForRow = row.get(orderColumn.getPosition());
    if (!orderIdentifier.equals(orderIdForRow)) {
      throw new FulfillmentException("Shipment file contains inconsistent order numbers.");
    }
  }

  private void initializeExtraFields(FileTemplate template) {
    extraDataFields = template
        .getFileColumns().stream()
        .filter(i -> !FileColumnKeyPath.ALL_REQUIRED_COLUMN_PATHS
            .contains(i.getFileColumnKeyPathEnum()))
        .collect(toList());
  }

  private void initializeOrderableMaps() {
    List<OrderableDto> orderables = orderableService.findAll();

    productCodeOrderableMap = orderables.stream()
        .collect(toMap(OrderableDto::getProductCode, orderable -> orderable));

    uuidOrderableMap = orderables.stream()
        .collect(toMap(OrderableDto::getId, orderable -> orderable));
  }

  private void validateOrderableAndQuantity(UUID orderableId, Long quantityShipped) {
    if (orderableId == null) {
      throw new FulfillmentException("Orderable not found for line Item.");
    }
    if (quantityShipped < 0) {
      throw new FulfillmentException(String
          .format("Quantity Shipped for %s should be number greater than or equal to 0.",
              orderableId));
    }
  }

  private UUID extractOrderableId(FileColumn orderableColumn, CSVRecord row) {
    if (FileColumnKeyPath.ORDERABLE_ID.equals(orderableColumn.getFileColumnKeyPathEnum())) {
      String orderableIdString = row.get(orderableColumn.getPosition());
      UUID orderableId = UUID.fromString(orderableIdString);
      return (uuidOrderableMap.containsKey(orderableId)) ? orderableId : null;
    } else if (FileColumnKeyPath.PRODUCT_CODE.equals(orderableColumn.getFileColumnKeyPathEnum())) {
      String productCode = row.get(orderableColumn.getPosition());
      OrderableDto orderableDto = productCodeOrderableMap.get(productCode);
      return (orderableDto != null) ? orderableDto.getId() : null;
    }
    return null;
  }

  private Map<String, String> extractExtraData(CSVRecord row) {
    Map<String, String> extraData = new HashMap<>();
    if (!extraDataFields.isEmpty()) {
      for (FileColumn column : extraDataFields) {
        extraData.put(column.getKeyPath(), row.get(column.getPosition()));
      }
    }
    return extraData;
  }

}
