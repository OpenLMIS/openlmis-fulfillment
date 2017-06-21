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

package org.openlmis.fulfillment.web.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProofOfDeliveryLineItemDto implements ProofOfDeliveryLineItem.Importer,
    ProofOfDeliveryLineItem.Exporter {

  private UUID id;
  private OrderLineItemDto orderLineItem;
  private Long packsToShip;
  private Long quantityShipped;
  private Long quantityReceived;
  private Long quantityReturned;
  private String replacedProductCode;
  private String notes;

  /**
   * Create new instance of ProofOfDeliveryLineItemDto based of given
   * {@link ProofOfDeliveryLineItem}
   * @param proofOfDeliveryLineItem instance of {@link ProofOfDeliveryLineItem}
   * @return new instance of ProofOfDeliveryLineItemDto.
   */
  public static ProofOfDeliveryLineItemDto newInstance(
          ProofOfDeliveryLineItem proofOfDeliveryLineItem, ExporterBuilder exporter,
          List<OrderableDto> orderables) {

    OrderLineItemDto orderLineItemDto = new OrderLineItemDto();
    exporter.export(proofOfDeliveryLineItem.getOrderLineItem(), orderLineItemDto, orderables);

    ProofOfDeliveryLineItemDto proofOfDeliveryLineItemDto = new ProofOfDeliveryLineItemDto();
    proofOfDeliveryLineItem.export(proofOfDeliveryLineItemDto);

    proofOfDeliveryLineItemDto.setOrderLineItem(orderLineItemDto);

    return proofOfDeliveryLineItemDto;
  }
}
