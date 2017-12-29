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

package org.openlmis.fulfillment.web.shipment;

import static org.openlmis.fulfillment.service.ResourceNames.ORDERS;
import static org.openlmis.fulfillment.service.ResourceNames.SHIPMENTS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.fulfillment.domain.CreationDetails;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import org.openlmis.fulfillment.web.util.UserObjectReferenceDto;
import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"serviceUrl"})
@ToString
public final class ShipmentDto implements CreationDetails.Exporter,
    Shipment.Exporter, Shipment.Importer {

  @Setter
  private String serviceUrl;

  @Setter
  @Getter
  private UUID id;

  @Getter
  private ObjectReferenceDto order;

  @Setter
  @Getter
  private UserObjectReferenceDto shippedBy;

  @Setter
  @Getter
  private ZonedDateTime shippedDate;

  @Setter
  @Getter
  private String notes;

  @Override
  @JsonIgnore
  public void setUserId(UUID updaterId) {
    if (updaterId != null) {
      shippedBy = UserObjectReferenceDto.create(updaterId, serviceUrl, SHIPMENTS);
    }
  }

  @Override
  @JsonIgnore
  public void setDate(ZonedDateTime updatedDate) {
    shippedDate = updatedDate;
  }

  public void setOrder(ObjectReferenceDto order) {
    this.order = order;
  }

  @Override
  @JsonIgnore
  public void setOrder(Order order) {
    if (order != null) {
      this.order = ObjectReferenceDto.create(order.getId(), serviceUrl, ORDERS);
    }
  }

  @Override
  @JsonIgnore
  public void setShipDetails(CreationDetails creationDetails) {
    creationDetails.export(this);
  }

  @Override
  @JsonIgnore
  public CreationDetails getShipDetails() {
    return new CreationDetails(shippedBy.getId(), shippedDate);
  }
}