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

package org.openlmis.fulfillment.testutils;

import org.openlmis.fulfillment.domain.ShipmentLineItem;
import java.util.UUID;

public class ShipmentLineItemDataBuilder {

  private UUID id = UUID.randomUUID();
  private UUID orderableId = UUID.randomUUID();
  private UUID lotId = UUID.randomUUID();
  private Long quantityShipped = 10L;

  public ShipmentLineItemDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public ShipmentLineItemDataBuilder withOrderableId(UUID id) {
    this.orderableId = id;
    return this;
  }

  public ShipmentLineItemDataBuilder withLotId(UUID id) {
    this.lotId = id;
    return this;
  }

  public ShipmentLineItemDataBuilder withQuantityShipped(Long quantityShipped) {
    this.quantityShipped = quantityShipped;
    return this;
  }

  public ShipmentLineItemDataBuilder withoutId() {
    this.id = null;
    return this;
  }

  public ShipmentLineItemDataBuilder withoutLotId() {
    this.lotId = null;
    return this;
  }

  /**
   * Builds instance of {@link ShipmentLineItem}.
   */
  public ShipmentLineItem build() {
    ShipmentLineItem line = new ShipmentLineItem(orderableId, lotId, quantityShipped);
    line.setId(id);
    return line;
  }
}
