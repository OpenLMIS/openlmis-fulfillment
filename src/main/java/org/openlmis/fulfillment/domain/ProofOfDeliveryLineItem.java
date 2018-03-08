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

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_INCORRECT_QUANTITIES;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_INCORRECT_VVM_STATUS;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_MISSING_REASON;

import org.hibernate.annotations.Type;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.fulfillment.domain.naming.VvmStatus;
import org.openlmis.fulfillment.web.ValidationException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Table(name = "proof_of_delivery_line_items")
@TypeName("ProofOfDeliveryLineItem")
public class ProofOfDeliveryLineItem extends BaseEntity {

  @Type(type = UUID_TYPE)
  @Column(nullable = false)
  @Getter
  private UUID orderableId;

  @Type(type = UUID_TYPE)
  @Getter(AccessLevel.PACKAGE)
  private UUID lotId;

  @Getter(AccessLevel.PACKAGE)
  private Integer quantityAccepted;

  @Column(nullable = false)
  @Getter(AccessLevel.PACKAGE)
  private Boolean useVvm;

  @Enumerated(EnumType.STRING)
  @Getter(AccessLevel.PACKAGE)
  private VvmStatus vvmStatus;

  @Getter(AccessLevel.PACKAGE)
  private Integer quantityRejected;

  @Getter(AccessLevel.PACKAGE)
  private UUID rejectionReasonId;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter(AccessLevel.PACKAGE)
  private String notes;

  ProofOfDeliveryLineItem(ShipmentLineItem shipmentLineItem, Boolean useVvm) {
    this(
        shipmentLineItem.getOrderableId(), shipmentLineItem.getLotId(), null, toBoolean(useVvm),
        null, null, null, null
    );
  }

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
   * Validate if this line item has correct values. The following validations will be done:
   * <ul>
   * <li><strong>quantityAccepted</strong> - must be zero or greater than zero</li>
   * <li><strong>quantityRejected</strong> - must be zero or greater than zero</li>
   * <li>if <strong>quantityAccepted</strong> is greater than zero and <strong>useVvm</strong> flag
   * is set, the <strong>vvmStatus</strong> must be less or equal to two</li>
   * <li>if <strong>quantityRejected</strong> is greater than zero, reason id must be provided</li>
   * <li>sum of <strong>quantityAccepted</strong> and <strong>quantityRejected</strong> must be
   * equals to <strong>quantityShipped</strong></li>
   * </ul>
   *
   * @param quantityShipped this value should be from related shipment line item.
   * @throws ValidationException if any validation does not match.
   */
  void validate(Long quantityShipped) {
    Validations.throwIfLessThanZeroOrNull(quantityAccepted, "quantityAccepted");
    Validations.throwIfLessThanZeroOrNull(quantityRejected, "quantityRejected");

    if (quantityAccepted > 0
        && useVvm
        && (null == vvmStatus || vvmStatus.isGreaterThan(2))) {
      throw new ValidationException(ERROR_INCORRECT_VVM_STATUS);
    }

    if (quantityRejected > 0 && null == rejectionReasonId) {
      throw new ValidationException(ERROR_MISSING_REASON);
    }

    if (quantityAccepted + quantityRejected != Math.toIntExact(quantityShipped)) {
      throw new ValidationException(ERROR_INCORRECT_QUANTITIES);
    }
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
        importer.getUseVvm(), importer.getVvmStatus(), importer.getQuantityRejected(),
        importer.getRejectionReasonId(), importer.getNotes()
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
    exporter.setUseVvm(useVvm);
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

    Boolean getUseVvm();

    VvmStatus getVvmStatus();

    Integer getQuantityRejected();

    UUID getRejectionReasonId();

    String getNotes();

  }

  public interface Exporter {
    void setId(UUID id);

    void setOrderableId(UUID orderableId);

    void setLotId(UUID lotId);

    void setQuantityAccepted(Integer quantityAccepted);

    void setUseVvm(Boolean useVvm);

    void setVvmStatus(VvmStatus vvmStatus);

    void setQuantityRejected(Integer quantityRejected);

    void setRejectionReasonId(UUID rejectionReasonId);

    void setNotes(String notes);

  }

}
