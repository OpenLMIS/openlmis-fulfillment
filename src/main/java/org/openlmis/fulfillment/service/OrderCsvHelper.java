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

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.apache.commons.collections.CollectionUtils.filter;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.commons.jxpath.JXPathContext;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderFileColumn;
import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderCsvHelper {
  private static final String STRING = "string";
  private static final String LINE_NO = "line_no";
  private static final String ORDER = "order";

  private static final String FACILITY = "Facility";
  private static final String PRODUCT = "Orderable";
  private static final String PERIOD = "ProcessingPeriod";

  private static final String LINE_SEPARATOR = "\r\n";
  private static final Boolean ENCLOSE_VALUES_WITH_QUOTES = false;

  @Autowired
  private FacilityReferenceDataService facilityReferenceDataService;

  @Autowired
  private PeriodReferenceDataService periodReferenceDataService;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  @Value("${order.export.includeZeroQuantity}")
  private boolean includeZeroQuantity;

  /**
   * Exporting order to csv.
   */
  public void writeCsvFile(Order order, OrderFileTemplate orderFileTemplate, Writer writer)
      throws IOException {
    List<OrderFileColumn> orderFileColumns = orderFileTemplate.getOrderFileColumns();
    removeExcludedColumns(orderFileColumns);
    if (orderFileTemplate.getHeaderInFile()) {
      writeHeader(orderFileColumns, writer);
    }

    writeLineItems(order, order.getOrderLineItems(), orderFileColumns, writer);
  }

  private void removeExcludedColumns(List<OrderFileColumn> orderFileColumns) {
    filter(orderFileColumns, object -> ((OrderFileColumn) object).getInclude());
  }

  private void writeHeader(List<OrderFileColumn> orderFileColumns, Writer writer)
      throws IOException {
    for (OrderFileColumn column : orderFileColumns) {
      String columnLabel = column.getColumnLabel();
      if (columnLabel == null) {
        columnLabel = "";
      }
      writer.write(columnLabel);
      if (orderFileColumns.indexOf(column) == (orderFileColumns.size() - 1)) {
        writer.write(LINE_SEPARATOR);
        break;
      }
      writer.write(",");
    }
  }

  private void writeLineItems(Order order, List<OrderLineItem> orderLineItems,
                              List<OrderFileColumn> orderFileColumns, Writer writer)
      throws IOException {
    int counter = 1;
    for (OrderLineItem orderLineItem : orderLineItems) {
      if (includeZeroQuantity || orderLineItem.getOrderedQuantity() > 0) {
        writeCsvLineItem(order, orderLineItem, orderFileColumns, writer, counter++);
        writer.write(LINE_SEPARATOR);
      }
    }
  }

  private void writeCsvLineItem(Order order, OrderLineItem orderLineItem,
                                List<OrderFileColumn> orderFileColumns, Writer writer, int counter)
      throws IOException {
    JXPathContext orderContext = JXPathContext.newContext(order);
    JXPathContext lineItemContext = JXPathContext.newContext(orderLineItem);
    for (OrderFileColumn orderFileColumn : orderFileColumns) {
      if (orderFileColumn.getNested() == null || orderFileColumn.getNested().isEmpty()) {
        if (orderFileColumns.indexOf(orderFileColumn) < orderFileColumns.size() - 1) {
          writer.write(",");
        }
        continue;
      }
      Object columnValue = getColumnValue(counter, orderContext, lineItemContext, orderFileColumn);

      if (columnValue instanceof ZonedDateTime) {
        columnValue = ((ZonedDateTime) columnValue).format(ofPattern(orderFileColumn.getFormat()));
      } else if (columnValue instanceof LocalDate) {
        columnValue = ((LocalDate) columnValue).format(ofPattern(orderFileColumn.getFormat()));
      }
      if (ENCLOSE_VALUES_WITH_QUOTES) {
        writer.write("\"" + (columnValue).toString() + "\"");
      } else {
        writer.write((columnValue).toString());
      }
      if (orderFileColumns.indexOf(orderFileColumn) < orderFileColumns.size() - 1) {
        writer.write(",");
      }
    }
  }

  private Object getColumnValue(int counter, JXPathContext orderContext,
                                JXPathContext lineItemContext, OrderFileColumn orderFileColumn) {
    Object columnValue;

    switch (orderFileColumn.getNested()) {
      case STRING:
        columnValue = orderFileColumn.getKeyPath();
        break;
      case LINE_NO:
        columnValue = counter;
        break;
      case ORDER:
        columnValue = orderContext.getValue(orderFileColumn.getKeyPath());
        break;
      default:
        columnValue = lineItemContext.getValue(orderFileColumn.getKeyPath());
        break;
    }

    if (orderFileColumn.getRelated() != null && !orderFileColumn.getRelated().isEmpty()) {
      columnValue = getRelatedColumnValue((UUID) columnValue, orderFileColumn);
    }

    return columnValue == null ? "" : columnValue;
  }

  private Object getRelatedColumnValue(UUID relatedId, OrderFileColumn orderFileColumn) {
    if (relatedId == null) {
      return null;
    }

    Object columnValue;

    switch (orderFileColumn.getRelated()) {
      case FACILITY:
        FacilityDto facility = facilityReferenceDataService.findOne(relatedId);
        columnValue = getValue(facility, orderFileColumn.getRelatedKeyPath());
        break;
      case PRODUCT:
        OrderableDto product = orderableReferenceDataService.findOne(relatedId);
        columnValue = getValue(product, orderFileColumn.getRelatedKeyPath());
        break;
      case PERIOD:
        ProcessingPeriodDto period = periodReferenceDataService.findOne(relatedId);
        columnValue = getValue(period, orderFileColumn.getRelatedKeyPath());
        break;
      default:
        columnValue = null;
        break;
    }

    return columnValue;
  }

  private Object getValue(Object object, String keyPath) {
    JXPathContext context = JXPathContext.newContext(object);

    return context.getValue(keyPath);
  }
}
