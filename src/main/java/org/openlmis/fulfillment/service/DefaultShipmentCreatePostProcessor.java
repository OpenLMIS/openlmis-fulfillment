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

import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.extension.point.ShipmentCreatePostProcessor;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Component;

@Component("DefaultShipmentCreatePostProcessor")
public class DefaultShipmentCreatePostProcessor implements ShipmentCreatePostProcessor {

  private static final XLogger XLOGGER = XLoggerFactory
      .getXLogger(DefaultShipmentCreatePostProcessor.class);

  private final StockEventStockManagementService stockEventService;

  private final StockEventBuilder stockEventBuilder;

  public DefaultShipmentCreatePostProcessor(
      StockEventStockManagementService stockEventService,
      StockEventBuilder stockEventBuilder) {
    this.stockEventService = stockEventService;
    this.stockEventBuilder = stockEventBuilder;
  }

  @Override
  public void process(Shipment shipment) {
    XLOGGER.entry(shipment);
    Profiler profiler = new Profiler("DEFAULT_SHIPMENT_CREATE_POST_PROCESSOR");
    profiler.setLogger(XLOGGER);

    profiler.start("BUILD_STOCK_EVENT_FROM_SHIPMENT");
    StockEventDto stockEventDto = stockEventBuilder.fromShipment(shipment);

    profiler.start("SUBMIT_STOCK_EVENT");
    stockEventService.submit(stockEventDto);

    profiler.stop().log();
    XLOGGER.exit();
  }
}
