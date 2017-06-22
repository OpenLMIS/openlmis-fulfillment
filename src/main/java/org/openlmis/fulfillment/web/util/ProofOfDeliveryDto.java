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

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.service.ExporterBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProofOfDeliveryDto implements ProofOfDelivery.Exporter, ProofOfDelivery.Importer {
  private UUID id;
  private OrderDto order;
  private List<ProofOfDeliveryLineItemDto> proofOfDeliveryLineItems;
  private String deliveredBy;
  private String receivedBy;
  private ZonedDateTime receivedDate;

  @Override
  public List<ProofOfDeliveryLineItem.Importer> getProofOfDeliveryLineItems() {
    return new ArrayList<>(
        Optional.ofNullable(proofOfDeliveryLineItems).orElse(Collections.emptyList())
    );
  }

  /**
   * Create new list of ProofOfDeliveryDto based on given list of {@link ProofOfDelivery}
   * @param proofOfDeliveries instance of ProofOfDelivery
   * @return new instance ProofOfDeliveryDto.
   */
  public static Collection<ProofOfDeliveryDto> newInstance(
      Iterable<ProofOfDelivery> proofOfDeliveries, ExporterBuilder exporter) {

    Collection<ProofOfDeliveryDto> proofOfDeliveryDtos = new ArrayList<>();
    proofOfDeliveries.forEach(pod -> proofOfDeliveryDtos.add(newInstance(pod, exporter)));
    return proofOfDeliveryDtos;
  }

  /**
   * Create new instance of ProofOfDeliveryDto based on given {@link ProofOfDelivery}
   * @param proofOfDelivery instance of ProofOfDelivery
   * @return new instance od ProofOfDeliveryDto.
   */
  public static ProofOfDeliveryDto newInstance(ProofOfDelivery proofOfDelivery,
                                               ExporterBuilder exporter) {
    ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
    proofOfDelivery.export(proofOfDeliveryDto);

    proofOfDeliveryDto.setOrder(OrderDto.newInstance(proofOfDelivery.getOrder(), exporter));

    if (proofOfDelivery.getProofOfDeliveryLineItems() != null) {
      List<OrderableDto> orderables = exporter.getLineItemOrderables(proofOfDelivery.getOrder());

      proofOfDeliveryDto.setProofOfDeliveryLineItems(proofOfDelivery.getProofOfDeliveryLineItems()
          .stream().map(item -> ProofOfDeliveryLineItemDto.newInstance(item, exporter, orderables))
          .collect(Collectors.toList()));
    }
    return proofOfDeliveryDto;
  }
}
