package org.openlmis.fulfillment.domain;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "order_line_items")
@NoArgsConstructor
public class OrderLineItem extends BaseEntity {

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(name = "orderId", nullable = false)
  @Getter
  @Setter
  private Order order;

  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID orderableProductId;

  @Column(nullable = false)
  @Getter
  @Setter
  private Long orderedQuantity;

  @Column(nullable = false)
  @Getter
  @Setter
  private Long filledQuantity;

  @Column(nullable = false)
  @Getter
  @Setter
  private Long approvedQuantity;

  /**
   * Copy values of attributes into new or updated OrderLineItem.
   *
   * @param orderLineItem OrderLineItem with new values.
   */
  public void updateFrom(OrderLineItem orderLineItem) {
    this.order = orderLineItem.getOrder();
    this.orderableProductId = orderLineItem.getOrderableProductId();
    this.orderedQuantity = orderLineItem.getOrderedQuantity();
    this.filledQuantity = orderLineItem.getFilledQuantity();
    this.approvedQuantity = orderLineItem.getApprovedQuantity();
  }

  /**
   * Create new instance of OrderLineItem based on given {@link OrderLineItem.Importer}
   * @param importer instance of {@link OrderLineItem.Importer}
   * @return new instance of OrderLineItem.
   */
  public static OrderLineItem newInstance(Importer importer) {
    OrderLineItem orderLineItem = new OrderLineItem();
    orderLineItem.setId(importer.getId());
    orderLineItem.setOrderableProductId(importer.getOrderableProductId());
    orderLineItem.setOrderedQuantity(importer.getOrderedQuantity());
    orderLineItem.setFilledQuantity(importer.getFilledQuantity());
    orderLineItem.setApprovedQuantity(importer.getApprovedQuantity());
    return orderLineItem;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setApprovedQuantity(approvedQuantity);
    exporter.setOrderableProductId(orderableProductId);
    exporter.setFilledQuantity(filledQuantity);
    exporter.setOrderedQuantity(orderedQuantity);
  }

  public interface Exporter {
    void setId(UUID id);

    void setOrderableProductId(UUID orderableProductId);

    void setOrderedQuantity(Long orderedQuantity);

    void setFilledQuantity(Long filledQuantity);

    void setApprovedQuantity(Long approvedQuantity);

  }

  public interface Importer {
    UUID getId();

    UUID getOrderableProductId();

    Long getOrderedQuantity();

    Long getFilledQuantity();

    Long getApprovedQuantity();

  }
}
