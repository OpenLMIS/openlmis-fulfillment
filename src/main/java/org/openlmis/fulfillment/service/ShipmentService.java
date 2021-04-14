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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ShipmentService.class);

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Saves the given shipment to database. Also related Proof Of Delivery will be created.
   */
  public Shipment create(final Shipment shipment) {
    XLOGGER.entry();
    Profiler profiler = new Profiler("SAVE_SHIPMENT");
    profiler.setLogger(XLOGGER);

    profiler.start("SAVE_SHIPMENT_TO_DB");
    entityManager.persist(shipment);

    entityManager.flush();
    entityManager.clear();

    profiler.start("CREATE_POD");
    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(shipment);

    profiler.start("SAVE_POD_TO_DB");
    entityManager.persist(proofOfDelivery);

    entityManager.flush();
    entityManager.clear();

    profiler.stop().log();
    XLOGGER.exit();
    return shipment;
  }

}
