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

package org.openlmis.fulfillment.service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentLineItem;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.web.util.BaseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private OrderableReferenceDataService orderableReferenceDataService;

  /**
   * Saves the given shipment to database. Also related Proof Of Delivery will be created.
   */
  public Shipment save(final Shipment shipment) {
    Shipment saved = shipmentRepository.save(shipment);

    Set<UUID> orderableIds = saved
        .getLineItems()
        .stream()
        .map(ShipmentLineItem::getOrderableId)
        .collect(Collectors.toSet());

    Map<UUID, Boolean> useVvm = orderableReferenceDataService
        .findByIds(orderableIds)
        .stream()
        .collect(Collectors.toMap(BaseDto::getId, OrderableDto::useVvm));

    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(saved, useVvm);
    proofOfDeliveryRepository.save(proofOfDelivery);

    return saved;
  }

}
