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

import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.extension.ExtensionManager;
import org.openlmis.fulfillment.extension.point.ExtensionPointId;
import org.openlmis.fulfillment.extension.point.ShipmentCreatePostProcessor;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentDraftRepository;
import org.openlmis.fulfillment.util.DateHelper;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShipmentService {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ShipmentService.class);

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ShipmentDraftRepository shipmentDraftRepository;

  @Autowired
  private DateHelper dateHelper;

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Ships the order of the given shipment: saves the shipment with its Proof Of Delivery, marks
   * the order SHIPPED, removes its shipment drafts and runs the shipment create post processor,
   * which by default submits the issue stock event. Must run in the caller's transaction, so
   * that a failing stock event rolls back everything saved here.
   *
   * @param shipment the shipment to save; its order must be ready to be fulfilled
   * @param userId   id of the user shipping the order
   * @return the saved shipment
   */
  public Shipment ship(final Shipment shipment, UUID userId) {
    XLOGGER.entry(shipment, userId);
    Profiler profiler = new Profiler("SHIP");
    profiler.setLogger(XLOGGER);

    profiler.start("SAVE_SHIPMENT");
    final Shipment created = create(shipment);

    profiler.start("UPDATE_ORDER");
    Order order = shipment.getOrder();
    order.updateStatus(OrderStatus.SHIPPED,
        new UpdateDetails(userId, dateHelper.getCurrentDateTimeWithSystemZone()));
    orderRepository.save(order);

    profiler.start("REMOVE_DRAFT_SHIPMENTS");
    shipmentDraftRepository.findByOrder(order).forEach(shipmentDraftRepository::delete);

    // write everything before the stock event, which cannot be rolled back
    entityManager.flush();

    profiler.start("POST_PROCESS");
    ShipmentCreatePostProcessor shipmentCreatePostProcessor = extensionManager.getExtension(
        ExtensionPointId.SHIPMENT_CREATE_POST_POINT_ID, ShipmentCreatePostProcessor.class);
    shipmentCreatePostProcessor.process(created);

    profiler.stop().log();
    XLOGGER.exit(created);
    return created;
  }

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
