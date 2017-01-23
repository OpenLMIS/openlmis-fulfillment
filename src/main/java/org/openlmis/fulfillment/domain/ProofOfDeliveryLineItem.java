package org.openlmis.fulfillment.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "proof_of_delivery_line_items")
public class ProofOfDeliveryLineItem extends BaseEntity {

  @OneToOne
  @JoinColumn(name = "orderLineItemId", nullable = false)
  @Getter
  @Setter
  private OrderLineItem orderLineItem;

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "proofOfDeliveryId", nullable = false)
  @Getter
  @Setter
  private ProofOfDelivery proofOfDelivery;

  @Getter
  @Setter
  private Long quantityShipped;

  @Getter
  @Setter
  private Long quantityReceived;

  @Getter
  @Setter
  private Long quantityReturned;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String replacedProductCode;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String notes;

  public ProofOfDeliveryLineItem(ProofOfDelivery parent, OrderLineItem item) {
    this.proofOfDelivery = parent;
    this.orderLineItem = item;
  }

  /**
   * Copy values of attributes into new or updated ProofOfDeliveryLineItem.
   *
   * @param proofOfDeliveryLineItem ProofOfDeliveryLineItem with new values.
   */
  public void updateFrom(ProofOfDeliveryLineItem proofOfDeliveryLineItem) {
    this.quantityReceived = proofOfDeliveryLineItem.getQuantityReceived();
    this.quantityReturned = proofOfDeliveryLineItem.getQuantityReturned();
    this.notes = proofOfDeliveryLineItem.getNotes();
  }

  /**
   * Create new instance of ProofOfDeliveryLineItem based on given
   * {@link ProofOfDeliveryLineItem.Importer}
   * @param importer instance of {@link ProofOfDeliveryLineItem.Importer}
   * @return instance of ProofOfDeliveryLineItem.
   */
  public static ProofOfDeliveryLineItem newInstance(Importer importer) {
    ProofOfDeliveryLineItem proofOfDeliveryLineItem = new ProofOfDeliveryLineItem();

    proofOfDeliveryLineItem.setId(importer.getId());
    proofOfDeliveryLineItem.setOrderLineItem(
        OrderLineItem.newInstance(importer.getOrderLineItem()));
    proofOfDeliveryLineItem.setQuantityShipped(importer.getQuantityShipped());
    proofOfDeliveryLineItem.setQuantityReceived(importer.getQuantityReceived());
    proofOfDeliveryLineItem.setQuantityReturned(importer.getQuantityReturned());
    proofOfDeliveryLineItem.setReplacedProductCode(importer.getReplacedProductCode());
    proofOfDeliveryLineItem.setNotes(importer.getNotes());

    return proofOfDeliveryLineItem;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setQuantityShipped(quantityShipped);
    exporter.setQuantityReceived(quantityReceived);
    exporter.setQuantityReturned(quantityReturned);
    exporter.setReplacedProductCode(replacedProductCode);
    exporter.setNotes(notes);
  }

  public interface Importer {
    UUID getId();

    OrderLineItem.Importer getOrderLineItem();

    Long getQuantityShipped();

    Long getQuantityReceived();

    Long getQuantityReturned();

    String getReplacedProductCode();

    String getNotes();

  }

  public interface Exporter {
    void setId(UUID id);

    void setQuantityShipped(Long quantityShipped);

    void setQuantityReceived(Long quantityReceived);

    void setQuantityReturned(Long quantityReturned);

    void setReplacedProductCode(String replacedProductCode);

    void setNotes(String notes);

  }

}
