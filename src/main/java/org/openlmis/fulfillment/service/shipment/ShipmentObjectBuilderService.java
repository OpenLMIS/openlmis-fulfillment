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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.openlmis.fulfillment.domain.CreationDetails;
import org.openlmis.fulfillment.domain.FileColumn;
import org.openlmis.fulfillment.domain.FileTemplate;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.FulfillmentException;
import org.openlmis.fulfillment.util.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class ShipmentObjectBuilderService {

  private static final String ORDER_CODE = "orderCode";
  private static final String ORDERABLE_ID = "orderableId";
  private static final String QUANTITY_SHIPPED = "quantityShipped";

  private static final List<String> REQUIRED_FIELDS = asList(ORDER_CODE,
      ORDERABLE_ID, QUANTITY_SHIPPED);

  @Value("${shipment.shippedById}")
  UUID shippedById;

  @Autowired
  OrderRepository orderRepository;

  @Autowired
  DateHelper dateHelper;

  Map<String, Integer> requiredFieldKeys;

  Map<String, Integer> extraDataFields;

  /**
   * Creates a shipment domain object from parsed shipment csv data.
   */
  public Shipment build(FileTemplate template, List<Object[]> parsedData) {
    if (parsedData.isEmpty()) {
      throw new FulfillmentException("Parsed data is empty");
    }

    initializeFieldIndexes(template);

    //find the order number
    Object[] firstRow = parsedData.get(0);
    String orderCode = firstRow[requiredFieldKeys.get(ORDER_CODE)].toString().trim();
    Order order = orderRepository.findByOrderCode(orderCode);

    if (order == null) {
      throw new FulfillmentException("Order not found with code: " + orderCode);
    }

    List<ShipmentLineItem> lineItems = new ArrayList<>();
    for (Object[] row : parsedData) {
      UUID orderableId = UUID
          .fromString(row[requiredFieldKeys.get(ORDERABLE_ID)].toString().trim());
      Long quantityShipped = Long
          .parseLong(row[requiredFieldKeys.get(QUANTITY_SHIPPED)].toString().trim());
      Map<String, String> extraData = extractExtraData(row, extraDataFields);
      ShipmentLineItem lineItem = new ShipmentLineItem(orderableId, quantityShipped, extraData);
      lineItems.add(lineItem);
    }

    return new Shipment(order,
        new CreationDetails(shippedById,
            dateHelper.getCurrentDateTimeWithSystemZone()), null, lineItems, null);
  }

  private void initializeFieldIndexes(FileTemplate template) {
    requiredFieldKeys = template
        .getFileColumns().stream().filter(i -> REQUIRED_FIELDS.contains(i.getKeyPath())).collect(
            Collectors.toMap(FileColumn::getKeyPath, FileColumn::getPosition));

    extraDataFields = template
        .getFileColumns().stream().filter(i -> !REQUIRED_FIELDS.contains(i.getKeyPath())).collect(
            Collectors.toMap(FileColumn::getKeyPath, FileColumn::getPosition));
  }

  private Map<String, String> extractExtraData(Object[] row, Map<String, Integer> headers) {
    Map<String, String> extraData = new HashMap<>();
    if (headers.size() > 0) {
      for (Entry<String, Integer> entry : headers.entrySet()) {
        extraData.put(entry.getKey(), row[entry.getValue()].toString());
      }
    }
    return extraData;
  }
}
