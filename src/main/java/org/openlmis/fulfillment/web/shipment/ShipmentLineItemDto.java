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

import static org.openlmis.fulfillment.service.ResourceNames.LOTS;
import static org.openlmis.fulfillment.service.ResourceNames.ORDERABLES;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"serviceUrl"})
@ToString
public final class ShipmentLineItemDto
    implements ShipmentLineItem.Exporter, ShipmentLineItem.Importer {

  @Setter
  private String serviceUrl;

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private ObjectReferenceDto orderable;

  @Getter
  @Setter
  private ObjectReferenceDto lot;

  @Getter
  @Setter
  private Long quantityShipped;

  @Override
  @JsonIgnore
  public void setOrderableId(UUID orderableId) {
    if (orderableId != null) {
      this.orderable = ObjectReferenceDto.create(orderableId, serviceUrl, ORDERABLES);
    }
  }

  @Override
  @JsonIgnore
  public void setLotId(UUID lotId) {
    if (lotId != null) {
      this.lot = ObjectReferenceDto.create(lotId, serviceUrl, LOTS);
    }
  }

  @Override
  @JsonIgnore
  public UUID getOrderableId() {
    if (orderable == null) {
      return null;
    }
    return orderable.getId();
  }

  @Override
  @JsonIgnore
  public UUID getLotId() {
    if (lot == null) {
      return null;
    }
    return lot.getId();
  }
}
