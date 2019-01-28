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

package org.openlmis.fulfillment.repository;

import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.javers.core.Javers;
import org.javers.core.commit.CommitMetadata;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.SnapshotType;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.junit.Test;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.ProofOfDeliveryDataBuilder;
import org.openlmis.fulfillment.ProofOfDeliveryLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryLineItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("PMD.TooManyMethods")
public class ProofOfDeliveryRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<ProofOfDelivery> {

  private static final String EXPECTED_NEW_POD_SNAPSHOT =
      "There should be new snapshot for PoD";
  private static final String NOT_EXPECTED_NEW_POD_SNAPSHOT =
      "There should not be new snapshot for PoD";

  private static final String EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT =
      "There should be new snapshot for PoD line item";
  private static final String NOT_EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT =
      "There should not be new snapshot for PoD line item";

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private Javers javers;

  @Override
  ProofOfDeliveryRepository getRepository() {
    return this.proofOfDeliveryRepository;
  }

  @Override
  ProofOfDelivery generateInstance() {
    Order order = new OrderDataBuilder()
        .withoutId()
        .withoutLineItems()
        .build();
    Shipment shipment = new ShipmentDataBuilder()
        .withoutId()
        .withoutLineItems()
        .withOrder(order)
        .build();
    ProofOfDelivery pod = new ProofOfDeliveryDataBuilder()
        .withShipment(shipment)
        .withoutReceivedDate()
        .withoutReceivedBy()
        .withoutDeliveredBy()
        .withLineItems(Lists.newArrayList(
            new ProofOfDeliveryLineItemDataBuilder()
                .withoutQuantityAccepted()
                .withoutQuantityRejected()
                .build()
        ))
        .buildAsNew();

    orderRepository.save(order);
    shipmentRepository.save(shipment);

    return pod;
  }

  @Override
  void assertInstance(ProofOfDelivery instance) {
    super.assertInstance(instance);

    JqlQuery jqlQuery = QueryBuilder.byInstanceId(instance.getId(), ProofOfDelivery.class).build();
    List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);

    assertThat(snapshots, hasSize(greaterThan(0)));
  }

  @Test
  public void testDeleteWithLine() {
    ProofOfDelivery instance = generateInstance();
    assertNotNull(instance);

    instance = proofOfDeliveryRepository.save(instance);
    assertInstance(instance);

    UUID instanceId = instance.getId();

    proofOfDeliveryRepository.delete(instanceId);

    assertFalse(proofOfDeliveryRepository.exists(instanceId));
  }

  @Test
  public void shouldLogProofOfDeliveryCreation() {
    ProofOfDelivery pod = generateInstance();
    ProofOfDeliveryLineItem lineItem = pod.getLineItems().get(0);

    // new PoD
    proofOfDeliveryRepository.save(pod);

    List<CdoSnapshot> podSnapshots = getSnapshots(pod.getId(), ProofOfDelivery.class);
    List<CdoSnapshot> lineSnapshots = getSnapshots(lineItem.getId(), ProofOfDeliveryLineItem.class);

    assertThat(EXPECTED_NEW_POD_SNAPSHOT, podSnapshots, hasSize(1));
    verifyAuditLog(podSnapshots.get(0), SnapshotType.INITIAL);
    assertThat(EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT, lineSnapshots, hasSize(1));
    verifyAuditLog(lineSnapshots.get(0), SnapshotType.INITIAL);
  }

  @Test
  public void shouldLogChangesInProofOfDelivery() {
    ProofOfDelivery pod = generateInstance();
    ProofOfDeliveryLineItem lineItem = pod.getLineItems().get(0);

    ProofOfDeliveryDto podDto = new ProofOfDeliveryDto();
    ProofOfDeliveryLineItemDto lineItemDto = new ProofOfDeliveryLineItemDto();

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    podDto.setReceivedBy("Test receiver");
    podDto.setDeliveredBy("Test deliverer");
    podDto.setReceivedDate(LocalDate.now());
    pod = ProofOfDelivery.newInstance(podDto);

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    List<CdoSnapshot> podSnapshots = getSnapshots(pod.getId(), ProofOfDelivery.class);
    List<CdoSnapshot> lineSnapshots = getSnapshots(lineItem.getId(), ProofOfDeliveryLineItem.class);

    assertThat(EXPECTED_NEW_POD_SNAPSHOT, podSnapshots, hasSize(2));
    verifyAuditLog(
        podSnapshots.get(0),
        SnapshotType.UPDATE,
        new String[]{"receivedBy", "deliveredBy", "receivedDate"},
        new Object[]{podDto.getReceivedBy(), podDto.getDeliveredBy(), podDto.getReceivedDate()}
    );

    assertThat(NOT_EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT, lineSnapshots, hasSize(1));
  }

  @Test
  public void shouldLogChangesInProofOfDeliveryLineItem() {
    ProofOfDelivery pod = generateInstance();
    ProofOfDeliveryLineItem lineItem = pod.getLineItems().get(0);

    ProofOfDeliveryDto podDto = new ProofOfDeliveryDto();
    ProofOfDeliveryLineItemDto lineItemDto = new ProofOfDeliveryLineItemDto();

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    lineItemDto.setQuantityAccepted(15);
    lineItemDto.setQuantityRejected(5);

    ReflectionTestUtils.setField(podDto, "lineItems", Lists.newArrayList(lineItemDto));

    pod = ProofOfDelivery.newInstance(podDto);

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    List<CdoSnapshot> podSnapshots = getSnapshots(pod.getId(), ProofOfDelivery.class);
    List<CdoSnapshot> lineSnapshots = getSnapshots(lineItem.getId(), ProofOfDeliveryLineItem.class);

    assertThat(NOT_EXPECTED_NEW_POD_SNAPSHOT, podSnapshots, hasSize(1));

    assertThat(EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT, lineSnapshots, hasSize(2));
    verifyAuditLog(
        lineSnapshots.get(0),
        SnapshotType.UPDATE,
        new String[]{"quantityAccepted", "quantityRejected"},
        new Object[]{lineItemDto.getQuantityAccepted(), lineItemDto.getQuantityRejected()}
    );
  }

  @Test
  public void shouldLogProofOfDeliveryConfirmation() {
    ProofOfDelivery pod = generateInstance();
    ProofOfDeliveryLineItem lineItem = pod.getLineItems().get(0);

    ProofOfDeliveryDto podDto = new ProofOfDeliveryDto();
    ProofOfDeliveryLineItemDto lineItemDto = new ProofOfDeliveryLineItemDto();

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    podDto.setStatus(ProofOfDeliveryStatus.CONFIRMED);
    pod = ProofOfDelivery.newInstance(podDto);

    proofOfDeliveryRepository.save(pod);

    pod.export(podDto);
    lineItem.export(lineItemDto);

    List<CdoSnapshot> podSnapshots = getSnapshots(pod.getId(), ProofOfDelivery.class);
    List<CdoSnapshot> lineSnapshots = getSnapshots(lineItem.getId(), ProofOfDeliveryLineItem.class);

    assertThat(EXPECTED_NEW_POD_SNAPSHOT, podSnapshots, hasSize(2));
    verifyAuditLog(
        podSnapshots.get(0),
        SnapshotType.UPDATE,
        new String[]{"status"},
        new Object[]{podDto.getStatus()}
    );

    assertThat(NOT_EXPECTED_NEW_POD_LINE_ITEM_SNAPSHOT, lineSnapshots, hasSize(1));
  }

  @Test
  public void shouldFindProofOfDeliveryByShipmentId() {
    List<ProofOfDelivery> list = Lists.newArrayList();

    for (int i = 0; i < 10; ++i) {
      list.add(generateInstance());
    }

    proofOfDeliveryRepository.save(list);

    for (ProofOfDelivery proofOfDelivery : list) {
      Page<ProofOfDelivery> found = proofOfDeliveryRepository.search(
          proofOfDelivery.getShipment().getId(), null, null, null, null, createPageable(10, 0));

      assertEquals(1, found.getTotalElements());
      assertThat(found, hasItem(hasProperty("id", is(proofOfDelivery.getId()))));
    }
  }

  @Test
  public void shouldFindProofOfDeliveryByOrderId() {
    List<ProofOfDelivery> list = Lists.newArrayList();

    for (int i = 0; i < 10; ++i) {
      list.add(generateInstance());
    }

    proofOfDeliveryRepository.save(list);

    for (ProofOfDelivery proofOfDelivery : list) {
      Page<ProofOfDelivery> found = proofOfDeliveryRepository.search(
          null, proofOfDelivery.getShipment().getOrder().getId(), null, null, null,
          createPageable(10, 0));

      assertEquals(1, found.getTotalElements());
      assertThat(found, hasItem(hasProperty("id", is(proofOfDelivery.getId()))));
    }
  }

  @Test
  public void shouldFindProofOfDeliveryByAllParameters() {
    List<ProofOfDelivery> list = Lists.newArrayList();

    for (int i = 0; i < 10; ++i) {
      list.add(generateInstance());
    }

    proofOfDeliveryRepository.save(list);

    for (ProofOfDelivery proofOfDelivery : list) {

      Page<ProofOfDelivery> found = proofOfDeliveryRepository.search(
          proofOfDelivery.getShipment().getId(),
          proofOfDelivery.getShipment().getOrder().getId(),
          singleton(proofOfDelivery.getReceivingFacilityId()),
          singleton(proofOfDelivery.getSupplyingFacilityId()),
          singleton(proofOfDelivery.getProgramId()),
          createPageable(10, 0));

      assertEquals(1, found.getTotalElements());
      assertThat(found, hasItem(hasProperty("id", is(proofOfDelivery.getId()))));
    }
  }

  private List<CdoSnapshot> getSnapshots(UUID id, Class type) {
    return javers.findSnapshots(QueryBuilder.byInstanceId(id, type).build());
  }

  private void verifyAuditLog(CdoSnapshot snapshot, SnapshotType type) {
    verifyAuditLog(snapshot, type, new String[0], new Object[0]);
  }

  private void verifyAuditLog(CdoSnapshot snapshot, SnapshotType type,
                              String[] fields, Object[] values) {
    assertThat(snapshot.getType(), is(type));

    CommitMetadata commit = snapshot.getCommitMetadata();
    assertThat(commit.getAuthor(), is(notNullValue()));
    assertThat(commit.getCommitDate(), is(notNullValue()));

    if (fields.length > 0) {
      assertThat(snapshot.getChanged(), hasSize(fields.length));
      assertThat(snapshot.getChanged(), hasItems(fields));

      assertThat(fields.length, is(values.length));

      for (int i = 0, length = fields.length; i < length; ++i) {
        assertThat(snapshot.getPropertyValue(fields[i]), is(values[i]));
      }
    }
  }
}
