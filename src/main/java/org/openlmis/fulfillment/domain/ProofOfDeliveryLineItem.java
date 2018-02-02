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

import org.hibernate.annotations.Type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Table(name = "proof_of_delivery_line_items")
public class ProofOfDeliveryLineItem extends BaseEntity {

  @Type(type = UUID_TYPE)
  @Column(nullable = false)
  private UUID orderableId;

  @Type(type = UUID_TYPE)
  private UUID lotId;

  private Integer quantityAccepted;

  private String vvmStatus;

  private Integer quantityRejected;

  private UUID rejectionReasonId;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String notes;

  /**
   * Copy values of attributes into new or updated ProofOfDeliveryLineItem.
   *
   * @param proofOfDeliveryLineItem ProofOfDeliveryLineItem with new values.
   */
  void updateFrom(ProofOfDeliveryLineItem proofOfDeliveryLineItem) {
    this.quantityAccepted = proofOfDeliveryLineItem.quantityAccepted;
    this.vvmStatus = proofOfDeliveryLineItem.vvmStatus;
    this.quantityRejected = proofOfDeliveryLineItem.quantityRejected;
    this.rejectionReasonId = proofOfDeliveryLineItem.rejectionReasonId;
    this.notes = proofOfDeliveryLineItem.notes;
  }

  /**
   * Create new instance of ProofOfDeliveryLineItem based on given
   * {@link ProofOfDeliveryLineItem.Importer}
   * @param importer instance of {@link ProofOfDeliveryLineItem.Importer}
   * @return instance of ProofOfDeliveryLineItem.
   */
  static ProofOfDeliveryLineItem newInstance(Importer importer) {
    ProofOfDeliveryLineItem lineItem = new ProofOfDeliveryLineItem(
        importer.getOrderableId(), importer.getLotId(), importer.getQuantityAccepted(),
        importer.getVvmStatus(), importer.getQuantityRejected(), importer.getRejectionReasonId(),
        importer.getNotes()
    );
    lineItem.setId(importer.getId());

    return lineItem;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setOrderableId(orderableId);
    exporter.setLotId(lotId);
    exporter.setQuantityAccepted(quantityAccepted);
    exporter.setVvmStatus(vvmStatus);
    exporter.setQuantityRejected(quantityRejected);
    exporter.setRejectionReasonId(rejectionReasonId);
    exporter.setNotes(notes);
  }

  public interface Importer {
    UUID getId();

    UUID getOrderableId();

    UUID getLotId();

    Integer getQuantityAccepted();

    String getVvmStatus();

    Integer getQuantityRejected();

    UUID getRejectionReasonId();

    String getNotes();

  }

  public interface Exporter {
    void setId(UUID id);

    void setOrderableId(UUID orderableId);

    void setLotId(UUID lotId);

    void setQuantityAccepted(Integer quantityAccepted);

    void setVvmStatus(String vvmStatus);

    void setQuantityRejected(Integer quantityRejected);

    void setRejectionReasonId(UUID rejectionReasonId);

    void setNotes(String notes);

  }

}
