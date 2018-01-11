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

package org.openlmis.fulfillment.domain;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.TypeName;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "shipment_draft_line_items")
@TypeName("ShipmentDraftLineItem")
@AllArgsConstructor
@ToString
public class ShipmentDraftLineItem extends BaseEntity {

  @Type(type = UUID_TYPE)
  @Column(nullable = false)
  private UUID orderableId;

  @Type(type = UUID_TYPE)
  private UUID lotId;

  private Long quantityShipped;

  // Constructor needed by framework. Use all args constructor to create new instance.
  private ShipmentDraftLineItem() {}

  /**
   * Creates new instance based on data from {@link ShipmentLineItem.Importer}
   *
   * @param importer instance of {@link ShipmentLineItem.Importer}
   * @return new instance of shipment draft line item.
   */
  protected static ShipmentDraftLineItem newInstance(ShipmentLineItem.Importer importer) {
    ShipmentDraftLineItem shipmentLineItem = new ShipmentDraftLineItem(
        importer.getOrderableId(), importer.getLotId(), importer.getQuantityShipped());
    shipmentLineItem.setId(importer.getId());
    return shipmentLineItem;
  }


  /**
   * Allows update existing draft line item
   *
   * @param newItem new item to update from.
   */
  public void updateFrom(ShipmentDraftLineItem newItem) {
    this.orderableId = newItem.orderableId;
    this.lotId = newItem.lotId;
    this.quantityShipped = newItem.quantityShipped;
  }

  /**
   * Returns a copy of line item.
   */
  public ShipmentDraftLineItem copy() {
    ShipmentDraftLineItem clone = new ShipmentDraftLineItem(orderableId, lotId, quantityShipped);
    clone.setId(id);

    return clone;
  }

  /**
   * Exports data from the given shipment draft to the instance that implement
   * {@link ShipmentLineItem.Exporter} interface.
   */
  public void export(ShipmentLineItem.Exporter exporter) {
    exporter.setId(getId());
    exporter.setOrderableId(orderableId);
    exporter.setLotId(lotId);
    exporter.setQuantityShipped(quantityShipped);
  }
}
