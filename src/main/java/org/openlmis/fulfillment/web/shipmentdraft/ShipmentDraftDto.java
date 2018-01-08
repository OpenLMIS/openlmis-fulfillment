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

package org.openlmis.fulfillment.web.shipmentdraft;

import static org.openlmis.fulfillment.service.ResourceNames.ORDERS;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ShipmentDraft;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.web.shipment.ShipmentLineItemDto;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"serviceUrl"})
@ToString
public final class ShipmentDraftDto implements ShipmentDraft.Exporter, ShipmentDraft.Importer {

  @Setter
  private String serviceUrl;

  @Getter
  @Setter
  private UUID id;

  @Getter
  private ObjectReferenceDto order;

  @Getter
  @Setter
  private String notes;

  @Setter
  private List<ShipmentLineItemDto> lineItems;

  @JsonProperty
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
  public List<ShipmentLineItem.Importer> getLineItems() {
    return new ArrayList<>(Optional.ofNullable(lineItems).orElse(Collections.emptyList())
    );
  }

}
