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
import lombok.Getter;
import lombok.ToString;
import org.javers.core.metamodel.annotation.TypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "shipments")
@TypeName("Shipment")
@AllArgsConstructor
@ToString
public class Shipment extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "orderid", unique = true)
  @Getter
  private Order order;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "userId", column = @Column(name = "shippedbyid", nullable = false)),
      @AttributeOverride(name = "date", column = @Column(name = "shippeddate", nullable = false))
      })
  private CreationDetails shipDetails;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String notes;

  @OneToMany(
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @JoinColumn(name = "shipmentid", nullable = false)
  private List<ShipmentLineItem> lineItems;

  private Shipment() {}

  public List<ShipmentLineItem> getLineItems() {
    return Collections.unmodifiableList(lineItems);
  }

  /**
   * Creates new instance based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of Shipment.
   */
  public static Shipment newInstance(Importer importer) {
    List<ShipmentLineItem> items = new ArrayList<>(importer.getLineItems().size());
    if (importer.getLineItems() != null) {
      importer.getLineItems().forEach(
          sli -> items.add(ShipmentLineItem.newInstance(sli)));
    }

    Shipment inventoryItem = new Shipment(
        new Order(importer.getOrder().getId()),
        importer.getShipDetails(),
        importer.getNotes(),
        items);
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

    List<ShipmentLineItem.Importer> getLineItems();
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
