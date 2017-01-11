package org.openlmis.fulfillment.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.openlmis.fulfillment.domain.convert.LocalDateTimePersistenceConverter;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order extends BaseEntity {

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID externalId;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean emergency;

  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID facilityId;

  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID processingPeriodId;

  @Getter
  @Setter
  @Convert(converter = LocalDateTimePersistenceConverter.class)
  private LocalDateTime createdDate;

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID createdById;

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID programId;

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID requestingFacilityId;

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID receivingFacilityId;

  @Column(nullable = false)
  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID supplyingFacilityId;

  @Column(nullable = false, unique = true, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Getter
  @Setter
  private String orderCode;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private OrderStatus status;

  @Column(nullable = false)
  @Getter
  @Setter
  private BigDecimal quotedCost;

  @OneToMany(
      mappedBy = "order",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Fetch(FetchMode.SELECT)
  @Getter
  @Setter
  private List<OrderLineItem> orderLineItems;

  @PrePersist
  private void prePersist() {
    this.createdDate = LocalDateTime.now();
    forEachLine(line -> line.setOrder(this));
  }

  @PreUpdate
  private void preUpdate() {
    forEachLine(line -> line.setOrder(this));
  }

  /**
   * Copy values of attributes into new or updated Order.
   *
   * @param order Order with new values.
   */
  public void updateFrom(Order order) {
    this.externalId = order.externalId;
    this.emergency = order.emergency;
    this.facilityId = order.facilityId;
    this.processingPeriodId = order.processingPeriodId;
    this.createdById = order.createdById;
    this.programId = order.programId;
    this.requestingFacilityId = order.requestingFacilityId;
    this.receivingFacilityId = order.receivingFacilityId;
    this.supplyingFacilityId = order.supplyingFacilityId;
    this.orderCode = order.orderCode;
    this.status = order.status;
    this.quotedCost = order.quotedCost;
  }

  public void forEachLine(Consumer<OrderLineItem> consumer) {
    Optional.ofNullable(orderLineItems)
        .ifPresent(list -> list.forEach(consumer));
  }

  /**
   * Create a new instance of Order based on data from {@link Order.Importer}
   *
   * @param importer instance of {@link Order.Importer}
   * @return new instance of requisition.
   */
  public static Order newInstance(Importer importer) {
    Order order = new Order();
    order.setId(importer.getId());
    order.setExternalId(importer.getExternalId());
    order.setEmergency(importer.getEmergency());
    order.setFacilityId(importer.getFacilityId());
    order.setProgramId(importer.getProgramId());
    order.setRequestingFacilityId(importer.getRequestingFacilityId());
    order.setReceivingFacilityId(importer.getReceivingFacilityId());
    order.setSupplyingFacilityId(importer.getSupplyingFacilityId());
    order.setOrderCode(importer.getOrderCode());
    order.setStatus(importer.getStatus());
    order.setQuotedCost(importer.getQuotedCost());
    order.setProcessingPeriodId(importer.getProcessingPeriodId());
    order.setCreatedDate(importer.getCreatedDate());
    order.setCreatedById(importer.getCreatedById());
    order.setSupplyingFacilityId(importer.getSupplyingFacilityId());
    order.setOrderLineItems(new ArrayList<>());

    if (importer.getOrderLineItems() != null) {
      importer.getOrderLineItems().forEach(
          oli -> order.getOrderLineItems().add(OrderLineItem.newInstance(oli)));
    }
    return order;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setExternalId(externalId);
    exporter.setEmergency(emergency);
    exporter.setFacilityId(facilityId);
    exporter.setProgramId(programId);
    exporter.setProcessingPeriodId(processingPeriodId);
    exporter.setRequestingFacilityId(requestingFacilityId);
    exporter.setReceivingFacilityId(receivingFacilityId);
    exporter.setSupplyingFacilityId(supplyingFacilityId);
    exporter.setOrderCode(orderCode);
    exporter.setStatus(status);
    exporter.setQuotedCost(quotedCost);
    exporter.setCreatedById(createdById);
    exporter.setCreatedDate(createdDate);
  }

  public interface Exporter {
    void setId(UUID id);

    void setExternalId(UUID id);

    void setEmergency(Boolean emergency);

    void setFacilityId(UUID facilityId);

    void setProgramId(UUID programId);

    void setRequestingFacilityId(UUID requestingFacilityId);

    void setReceivingFacilityId(UUID receivingFacilityId);

    void setSupplyingFacilityId(UUID supplyingFacilityId);

    void setOrderCode(String orderCode);

    void setStatus(OrderStatus orderStatus);

    void setQuotedCost(BigDecimal quotedCost);

    void setProcessingPeriodId(UUID id);

    void setCreatedDate(LocalDateTime localDateTime);

    void setCreatedById(UUID id);

  }

  public interface Importer {
    UUID getId();

    UUID getExternalId();

    Boolean getEmergency();

    UUID getFacilityId();

    UUID getProgramId();

    UUID getRequestingFacilityId();

    UUID getReceivingFacilityId();

    UUID getSupplyingFacilityId();

    String getOrderCode();

    OrderStatus getStatus();

    BigDecimal getQuotedCost();

    List<OrderLineItem.Importer> getOrderLineItems();

    UUID getProcessingPeriodId();

    LocalDateTime getCreatedDate();

    UUID getCreatedById();

  }
}
