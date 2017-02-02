package org.openlmis.fulfillment.domain;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.openlmis.fulfillment.domain.convert.LocalDateTimePersistenceConverter;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.web.util.StatusMessageDto;

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
  public static final String SUPPLYING_FACILITY_ID = "supplyingFacilityId";
  public static final String REQUESTING_FACILITY_ID = "requestingFacilityId";
  public static final String PROGRAM_ID = "programId";
  public static final String STATUS = "status";

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

  @OneToMany(
      mappedBy = "order",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      orphanRemoval = true)
  @Fetch(FetchMode.SELECT)
  @Getter
  @Setter
  private List<StatusMessage> statusMessages;

  @PrePersist
  private void prePersist() {
    this.createdDate = LocalDateTime.now();
    forEachLine(line -> line.setOrder(this));
    forEachStatus(status -> status.setOrder(this));
  }

  @PreUpdate
  private void preUpdate() {
    forEachLine(line -> line.setOrder(this));
    forEachStatus(status -> status.setOrder(this));
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

  public void forEachStatus(Consumer<StatusMessage> consumer) {
    Optional.ofNullable(statusMessages)
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

    Optional.ofNullable(importer.getFacility())
        .ifPresent(facility -> order.setFacilityId(facility.getId()));

    Optional.ofNullable(importer.getProgram())
        .ifPresent(program -> order.setProgramId(program.getId()));

    Optional.ofNullable(importer.getRequestingFacility())
        .ifPresent(facility -> order.setRequestingFacilityId(facility.getId()));

    Optional.ofNullable(importer.getReceivingFacility())
        .ifPresent(facility -> order.setReceivingFacilityId(facility.getId()));

    Optional.ofNullable(importer.getSupplyingFacility())
        .ifPresent(facility -> order.setSupplyingFacilityId(facility.getId()));

    order.setOrderCode(importer.getOrderCode());
    order.setStatus(importer.getStatus());
    order.setQuotedCost(importer.getQuotedCost());

    Optional.ofNullable(importer.getProcessingPeriod())
        .ifPresent(period -> order.setProcessingPeriodId(period.getId()));

    order.setCreatedDate(importer.getCreatedDate());

    Optional.ofNullable(importer.getCreatedBy())
        .ifPresent(user -> order.setCreatedById(user.getId()));

    order.setOrderLineItems(new ArrayList<>());
    order.setStatusMessages(new ArrayList<>());

    if (importer.getOrderLineItems() != null) {
      importer.getOrderLineItems().forEach(
          oli -> order.getOrderLineItems().add(OrderLineItem.newInstance(oli)));
    }

    if (importer.getStatusMessages() != null) {
      importer.getStatusMessages().forEach(
          sm -> order.getStatusMessages().add(StatusMessage.newInstance(sm)));
    }

    return order;
  }

  public interface Exporter {
    void setId(UUID id);

    void setExternalId(UUID id);

    void setEmergency(Boolean emergency);

    void setFacility(FacilityDto facility);

    void setProgram(ProgramDto program);

    void setRequestingFacility(FacilityDto requestingFacility);

    void setReceivingFacility(FacilityDto receivingFacility);

    void setSupplyingFacility(FacilityDto supplyingFacility);

    void setOrderCode(String orderCode);

    void setStatus(OrderStatus orderStatus);

    void setQuotedCost(BigDecimal quotedCost);

    void setProcessingPeriod(ProcessingPeriodDto period);

    void setCreatedDate(LocalDateTime localDateTime);

    void setCreatedBy(UserDto user);

    void setStatusMessages(List<StatusMessageDto> statusMessages);

  }

  public interface Importer {
    UUID getId();

    UUID getExternalId();

    Boolean getEmergency();

    FacilityDto getFacility();

    ProgramDto getProgram();

    FacilityDto getRequestingFacility();

    FacilityDto getReceivingFacility();

    FacilityDto getSupplyingFacility();

    String getOrderCode();

    OrderStatus getStatus();

    BigDecimal getQuotedCost();

    List<OrderLineItem.Importer> getOrderLineItems();

    List<StatusMessage.Importer> getStatusMessages();

    ProcessingPeriodDto getProcessingPeriod();

    LocalDateTime getCreatedDate();

    UserDto getCreatedBy();

  }
}
