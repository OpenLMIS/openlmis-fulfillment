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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.domain.ShipmentDraft;
import org.openlmis.fulfillment.domain.UpdateDetails;
import org.openlmis.fulfillment.extension.ExtensionManager;
import org.openlmis.fulfillment.extension.point.ExtensionPointId;
import org.openlmis.fulfillment.extension.point.ShipmentCreatePostProcessor;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentDraftRepository;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.util.DateHelper;

@RunWith(MockitoJUnitRunner.class)
public class ShipmentServiceTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ShipmentDraftRepository shipmentDraftRepository;

  @Mock
  private DateHelper dateHelper;

  @Mock
  private ExtensionManager extensionManager;

  @Mock
  private ShipmentCreatePostProcessor shipmentCreatePostProcessor;

  @InjectMocks
  private ShipmentService shipmentService;

  private final UUID userId = UUID.randomUUID();
  private final ZonedDateTime now = ZonedDateTime.now();
  private Order order;
  private Shipment shipment;

  @Before
  public void setUp() {
    order = new OrderDataBuilder().withOrderedStatus().build();
    shipment = new ShipmentDataBuilder().withOrder(order).build();
  }

  private void ship() {
    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(now);
    when(extensionManager.getExtension(ExtensionPointId.SHIPMENT_CREATE_POST_POINT_ID,
        ShipmentCreatePostProcessor.class)).thenReturn(shipmentCreatePostProcessor);

    shipmentService.ship(shipment, userId);
  }

  @Test
  public void shouldMarkOrderAsShippedWhenShipping() {
    ship();

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(orderRepository).save(captor.capture());
    assertThat(captor.getValue().getStatus(), is(OrderStatus.SHIPPED));
    assertThat(captor.getValue().getUpdateDetails(), is(new UpdateDetails(userId, now)));
  }

  @Test
  public void shouldSaveShipmentWithProofOfDeliveryWhenShipping() {
    ship();

    verify(entityManager).persist(shipment);
    verify(entityManager).persist(any(ProofOfDelivery.class));
  }

  @Test
  public void shouldRemoveDraftsWhenShipping() {
    ShipmentDraft draft = mock(ShipmentDraft.class);
    when(shipmentDraftRepository.findByOrder(order)).thenReturn(Collections.singletonList(draft));

    ship();

    verify(shipmentDraftRepository).delete(draft);
  }

  @Test
  public void shouldRunPostProcessorAfterSavingWhenShipping() {
    ship();

    InOrder inOrder = inOrder(entityManager, orderRepository, shipmentCreatePostProcessor);
    inOrder.verify(entityManager).persist(shipment);
    inOrder.verify(orderRepository).save(order);
    inOrder.verify(entityManager).flush();
    inOrder.verify(shipmentCreatePostProcessor).process(shipment);
  }

  @Test
  public void shouldSaveShipmentAndCreateProofOfDelivery() {
    // when
    shipmentService.create(shipment);

    // then
    verify(entityManager).persist(shipment);
    // creating a POD based on shipment and map is check in
    // ProofOfDeliveryTest.shouldCreateInstanceBasedOnShipment
    // here we only verify that POD has been saved
    verify(entityManager).persist(any(ProofOfDelivery.class));
  }
}
