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
import org.javers.core.metamodel.annotation.TypeName;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "shipments")
@TypeName("Shipment")
@AllArgsConstructor
@ToString
public class Shipment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "orderid")
  private Order order;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "userId", column = @Column(name = "shippedbyid", nullable = false)),
      @AttributeOverride(name = "date", column = @Column(name = "shippeddate", nullable = false))
      })
  private CreationDetails shipDetails;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String notes;

  private Shipment() {}

  /**
   * Creates new instance based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of Shipment.
   */
  public static Shipment newInstance(Importer importer) {
    Shipment inventoryItem = new Shipment(
        new Order(importer.getOrder().getId()),
        importer.getShipDetails(),
        importer.getNotes());
    inventoryItem.setId(importer.getId());

    return inventoryItem;
  }

  public interface Exporter {
    void setId(UUID id);

    void setOrder(Order order);

    void setShipDetails(CreationDetails creationDetails);

    void setNotes(String notes);
  }

  public interface Importer {
    UUID getId();

    Identifiable getOrder();

    CreationDetails getShipDetails();

    String getNotes();
  }

  /**
   * Exports data from the given shipment to the instance that implement
   * {@link Exporter} interface.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setOrder(order);
    exporter.setShipDetails(shipDetails);
    exporter.setNotes(notes);
  }

}
