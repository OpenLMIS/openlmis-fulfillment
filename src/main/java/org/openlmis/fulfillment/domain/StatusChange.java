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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "status_changes")
@NoArgsConstructor
public class StatusChange extends BaseEntity {

  @ManyToOne(cascade = {CascadeType.REFRESH})
  @JoinColumn(name = "orderId", nullable = false)
  @Getter
  @Setter
  private Order order;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Getter
  @Setter
  private ExternalStatus status;

  @Getter
  @Setter
  @Type(type = UUID_TYPE)
  private UUID authorId;

  @Column(columnDefinition = "timestamp with time zone")
  @Getter
  @Setter
  private ZonedDateTime createdDate;

  private StatusChange(ExternalStatus status, UUID authorId, ZonedDateTime date) {
    this.status = status;
    this.authorId = authorId;
    this.createdDate = date;
  }

  public static StatusChange newStatusChange(Importer importer) {
    return new StatusChange(importer.getStatus(),
            importer.getAuthorId(), importer.getCreatedDate());
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(StatusChange.Exporter exporter) {
    exporter.setCreatedDate(createdDate);
    exporter.setAuthorId(authorId);
    exporter.setStatus(status);
  }

  public interface Exporter {

    void setCreatedDate(ZonedDateTime date);

    void setAuthorId(UUID authorId);

    void setStatus(ExternalStatus status);
  }

  public interface Importer {

    UUID getAuthorId();

    ZonedDateTime getCreatedDate();

    ExternalStatus getStatus();
  }
}
