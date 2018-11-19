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

import lombok.NoArgsConstructor;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class ShipmentMessageHandler {

  private static final XLogger LOGGER = XLoggerFactory.getXLogger(ShipmentMessageHandler.class);


  /**
   * A message handler endpoint that processes incoming shipment files.
   *
   * @param message a file message.
   */
  public void process(Message message) {
    LOGGER.info("A message was received. " + message.getHeaders().getId());
  }

  /**
   * A message handler that handle error messages.
   *
   * @param mesage Error message that needs to be handled.
   */
  public void processError(Message mesage) {
    LOGGER.info("An error found: " + mesage.getHeaders().getId());
  }

}
