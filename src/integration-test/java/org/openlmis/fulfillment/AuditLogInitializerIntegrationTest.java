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

package org.openlmis.fulfillment;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang.StringUtils;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.GlobalId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.jql.QueryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentDraft;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.testutils.CreationDetailsDataBuilder;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles({"test", "init-audit-log", "test-run"})
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuditLogInitializerIntegrationTest {

  private static final String[] PROOFS_OF_DELIVERY_FIELDS = {
    "id", "shipmentid", "status", "deliveredby", "receivedby", "receiveddate"
  };

  private static final String[] SHIPMENT_FIELDS = {
      "id", "orderid", "shippedbyid", "shippeddate", "notes"
  };

  private static final String[] SHIPMENT_DRAFT_FIELDS = {
      "id", "orderid", "notes"
  };

  private static final String INSERT_POD_SQL = String.format(
      "INSERT INTO fulfillment.proofs_of_delivery (%s) VALUES (%s)",
      StringUtils.join(PROOFS_OF_DELIVERY_FIELDS, ", "),
      StringUtils.repeat("?", ",", PROOFS_OF_DELIVERY_FIELDS.length)
  );

  private static final String INSERT_SHIPMENT_SQL = String.format(
      "INSERT INTO fulfillment.shipments (%s) VALUES (%s) ",
      StringUtils.join(SHIPMENT_FIELDS, ", "),
      StringUtils.repeat("?", ",", SHIPMENT_FIELDS.length)
  );

  private static final String INSERT_SHIPMENT_DRAFT_SQL = String.format(
      "INSERT INTO fulfillment.shipment_drafts (%s) VALUES (%s) ",
      StringUtils.join(SHIPMENT_DRAFT_FIELDS, ", "),
      StringUtils.repeat("?", ", ", SHIPMENT_DRAFT_FIELDS.length)
  );

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private Javers javers;

  @Autowired
  private ApplicationContext applicationContext;

  @PersistenceContext
  private EntityManager entityManager;

  @Test
  public void shouldCreateSnapshotsForPoD() {
    //given
    UUID podId = UUID.randomUUID();

    Shipment shipment = addShipment();
    addProofOfDelivery(podId, shipment.getId());

    //when
    QueryBuilder jqlQuery = QueryBuilder.byInstanceId(podId, ProofOfDelivery.class);
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

    assertThat(snapshots, hasSize(0));

    AuditLogInitializer auditLogInitializer = new AuditLogInitializer(applicationContext, javers);
    auditLogInitializer.run();

    snapshots = javers.findSnapshots(jqlQuery.build());

    // then
    assertThat(snapshots, hasSize(1));

    CdoSnapshot snapshot = snapshots.get(0);
    GlobalId globalId = snapshot.getGlobalId();

    assertThat(globalId, is(notNullValue()));
    assertThat(globalId, instanceOf(InstanceId.class));

    InstanceId instanceId = (InstanceId) globalId;
    assertThat(instanceId.getCdoId(), is(podId));
    assertThat(instanceId.getTypeName(), is("ProofOfDelivery"));
  }

  @Test
  public void shouldCreateSnapshotsForShipment() {
    //given
    UUID shipmentId = UUID.randomUUID();

    Order order = addOrder();
    addShipmentQuery(shipmentId, order.getId());

    //when
    QueryBuilder jqlQuery = QueryBuilder.byInstanceId(shipmentId, Shipment.class);
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

    assertThat(snapshots, hasSize(0));

    AuditLogInitializer auditLogInitializer = new AuditLogInitializer(applicationContext, javers);
    auditLogInitializer.run();

    snapshots = javers.findSnapshots(jqlQuery.build());

    // then
    assertThat(snapshots, hasSize(1));

    CdoSnapshot snapshot = snapshots.get(0);
    GlobalId globalId = snapshot.getGlobalId();

    assertThat(globalId, is(notNullValue()));
    assertThat(globalId, instanceOf(InstanceId.class));

    InstanceId instanceId = (InstanceId) globalId;
    assertThat(instanceId.getCdoId(), is(shipmentId));
    assertThat(instanceId.getTypeName(), is("Shipment"));
  }

  @Test
  public void shouldCreateSnapshotsForShipmentDrafts() {
    //given
    UUID draftId = UUID.randomUUID();

    Order order = addOrder();
    addShipmentDraft(draftId, order.getId());

    //when
    QueryBuilder jqlQuery = QueryBuilder.byInstanceId(draftId, ShipmentDraft.class);
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

    assertThat(snapshots, hasSize(0));

    AuditLogInitializer auditLogInitializer = new AuditLogInitializer(applicationContext, javers);
    auditLogInitializer.run();

    snapshots = javers.findSnapshots(jqlQuery.build());

    // then
    assertThat(snapshots, hasSize(1));

    CdoSnapshot snapshot = snapshots.get(0);
    GlobalId globalId = snapshot.getGlobalId();

    assertThat(globalId, is(notNullValue()));
    assertThat(globalId, instanceOf(InstanceId.class));

    InstanceId instanceId = (InstanceId) globalId;
    assertThat(instanceId.getCdoId(), is(draftId));
    assertThat(instanceId.getTypeName(), is("ShipmentDraft"));
  }

  private Order addOrder() {
    Order order = new OrderDataBuilder().build();
    return orderRepository.save(order);
  }

  private Shipment addShipment() {
    Shipment shipment = new ShipmentDataBuilder()
        .withoutId()
        .withoutLineItems()
        .withShipDetails(new CreationDetailsDataBuilder()
            .build())
        .withOrder(addOrder())
        .build();

    return shipmentRepository.save(shipment);
  }

  private void addProofOfDelivery(UUID podId, UUID shipmentId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(INSERT_POD_SQL)
        .setParameter(1, podId) // podId
        .setParameter(2, shipmentId) //shipmentId
        .setParameter(3, ProofOfDeliveryStatus.INITIATED.name()) // status
        .setParameter(4, "") //deliveredBy
        .setParameter(5, "") //receivedBy
        .setParameter(6, LocalDate.now()) //receivedDate
        .executeUpdate();
  }

  private void addShipmentQuery(UUID shipmentId, UUID orderId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(INSERT_SHIPMENT_SQL)
        .setParameter(1, shipmentId) // shipmentId
        .setParameter(2, orderId) //orderId
        .setParameter(3, UUID.randomUUID()) //shippedById
        .setParameter(4, LocalDate.now()) //shippedDate
        .setParameter(5, "") //notes
        .executeUpdate();
  }

  private void addShipmentDraft(UUID draftId, UUID draftOrderId) {
    entityManager.flush();
    entityManager
        .createNativeQuery(INSERT_SHIPMENT_DRAFT_SQL)
        .setParameter(1, draftId) // draftId
        .setParameter(2, draftOrderId) //shipmentDraftOrderId
        .setParameter(3, "") //notes
        .executeUpdate();
  }
}
