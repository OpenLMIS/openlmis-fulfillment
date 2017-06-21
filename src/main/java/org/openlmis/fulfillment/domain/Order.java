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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.ProcessingPeriodDto;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.web.util.StatusChangeDto;
import org.openlmis.fulfillment.web.util.StatusMessageDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
  public static final String PROCESSING_PERIOD_ID = "processingPeriodId";

  @Column(nullable = false, unique = true)
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
  @Column(columnDefinition = "timestamp with time zone")
  private ZonedDateTime createdDate;

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
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Fetch(FetchMode.SELECT)
  @Getter
  @Setter
  private List<OrderLineItem> orderLineItems;

  @OneToMany(
      mappedBy = "order",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Fetch(FetchMode.SELECT)
  @Getter
  @Setter
  private List<StatusMessage> statusMessages;

  @OneToMany(
      mappedBy = "order",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE},
      fetch = FetchType.LAZY,
      orphanRemoval = true)
  @Getter
  @Setter
  private List<StatusChange> statusChanges;


  @PrePersist
  private void prePersist() {
    this.createdDate = ZonedDateTime.now();
    forEachLine(line -> line.setOrder(this));
    forEachStatus(status -> status.setOrder(this));
    forEachStatusChange(change -> change.setOrder(this));
  }

  @PreUpdate
  private void preUpdate() {
    forEachLine(line -> line.setOrder(this));
    forEachStatus(status -> status.setOrder(this));
    forEachStatusChange(change -> change.setOrder(this));
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
    this.statusChanges = order.statusChanges;
  }

  public void forEachLine(Consumer<OrderLineItem> consumer) {
    Optional.ofNullable(orderLineItems)
        .ifPresent(list -> list.forEach(consumer));
  }

  public void forEachStatus(Consumer<StatusMessage> consumer) {
    Optional.ofNullable(statusMessages)
        .ifPresent(list -> list.forEach(consumer));
  }

  public void forEachStatusChange(Consumer<StatusChange> consumer) {
    Optional.ofNullable(statusChanges)
            .ifPresent(list -> list.forEach(consumer));
  }

  /**
   * Create a new instance of Order based on data from {@link Order.Importer}
   *
   * @param importer instance of {@link Order.Importer}
   * @return new instance of order.
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
    order.setStatusChanges(new ArrayList<>());

    if (importer.getOrderLineItems() != null) {
      importer.getOrderLineItems().forEach(
          oli -> order.getOrderLineItems().add(OrderLineItem.newInstance(oli)));
    }

    if (importer.getStatusMessages() != null) {
      importer.getStatusMessages().forEach(
          sm -> order.getStatusMessages().add(StatusMessage.newInstance(sm)));
    }

    if (importer.getStatusChanges() != null) {
      importer.getStatusChanges().forEach(
          sch -> order.getStatusChanges().add(StatusChange.newStatusChange(sch)));
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

    void setCreatedDate(ZonedDateTime zonedDateTime);

    void setCreatedBy(UserDto user);

    void setStatusMessages(List<StatusMessageDto> statusMessages);

    void setStatusChanges(List<StatusChangeDto> statusChanges);
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

    ZonedDateTime getCreatedDate();

    UserDto getCreatedBy();

    List<StatusChange.Importer> getStatusChanges();
  }
}
