/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2026 VillageReach
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

package org.openlmis.fulfillment.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.domain.OrderType;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.ExternalApiException;
import org.openlmis.fulfillment.service.OrderSearchParams;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.PeriodReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.service.transfer.TransferPartnerService;
import org.openlmis.fulfillment.testutils.ProgramDataBuilder;
import org.openlmis.fulfillment.testutils.UserDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDtoDataBuilder;
import org.openlmis.fulfillment.web.util.LocalizedMessageDto;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.openlmis.fulfillment.web.util.TransferSendDto;
import org.openlmis.fulfillment.web.util.VersionIdentityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * POST /api/transfers/send against the real database, with the production transaction boundary.
 * Only permissions, transfer partners, stock management and reference data are mocked.
 */
@SuppressWarnings("PMD.UnusedPrivateField")
public class TransferSendIntegrationTest extends BaseWebIntegrationTest {

  private static final String TRANSFER_SEND_URL = "/api/transfers/send";
  private static final String SOH_EXCEEDED =
      "stockmanagement.error.event.debit.quantity.exceed.soh";
  private static final Pageable PAGE = PageRequest.of(0, 10);

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private TransferPartnerService transferPartnerService;

  @MockBean
  private AuthenticationHelper authenticationHelper;

  @MockBean
  private StockEventStockManagementService stockEventService;

  @MockBean
  private StockEventBuilder stockEventBuilder;

  @MockBean
  private ProgramReferenceDataService programReferenceDataService;

  @MockBean
  private FacilityReferenceDataService facilityReferenceDataService;

  @MockBean
  private OrderableReferenceDataService orderableReferenceDataService;

  @MockBean
  private PeriodReferenceDataService periodReferenceDataService;

  @MockBean
  private UserReferenceDataService userReferenceDataService;

  private final UserDto user = new UserDataBuilder().withId(INITIAL_USER_ID).build();
  private final UUID supplyingFacilityId = UUID.randomUUID();
  private final UUID receivingFacilityId = UUID.randomUUID();
  private final StockEventDto stockEvent = new StockEventDtoDataBuilder().build();
  private ProgramDto program;

  @Before
  public void setUp() {
    program = new ProgramDataBuilder().build();

    given(authenticationHelper.getCurrentUser()).willReturn(user);
    given(programReferenceDataService.findOne(program.getId())).willReturn(program);
    given(orderNumberConfigurationRepository.findAll()).willReturn(Collections.singletonList(
        new OrderNumberConfiguration("TP", true, true, true)));
    given(transferPartnerService
        .isValidPair(supplyingFacilityId, receivingFacilityId, program.getId()))
        .willReturn(true);
    given(stockEventBuilder.fromShipment(any(Shipment.class)))
        .willReturn(Optional.of(stockEvent));
  }

  @After
  public void cleanUp() {
    // the endpoint commits, so remove what the successful send saved
    inTransaction(() -> findPushedOrders().forEach(order -> {
      findProofsOfDelivery(order).forEach(proofOfDeliveryRepository::delete);
      findShipments(order).forEach(shipmentRepository::delete);
      orderRepository.delete(order);
    }));
  }

  @Test
  public void shouldSaveOrderShipmentAndProofOfDeliveryWhenStockEventIsAccepted() {
    restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferSend())
        .when()
        .post(TRANSFER_SEND_URL)
        .then()
        .statusCode(201);

    verify(stockEventService).submit(stockEvent);

    inTransaction(() -> {
      List<Order> pushed = findPushedOrders();
      assertThat(pushed, hasSize(1));
      Order order = pushed.get(0);
      assertThat(order.getStatus(), is(OrderStatus.SHIPPED));
      assertThat(order.getReceivingFacilityId(), is(receivingFacilityId));
      assertThat(order.getOrderLineItems(), hasSize(1));
      assertThat(order.getOrderLineItems().get(0).getOrderedQuantity(), is(7L));

      List<Shipment> shipments = findShipments(order);
      assertThat(shipments, hasSize(1));
      assertThat(shipments.get(0).getLineItems(), hasSize(2));

      assertThat(findProofsOfDelivery(order), hasSize(1));
    });
  }

  @Test
  public void shouldSaveNothingWhenStockEventIsRejected() {
    final long orders = orderRepository.count();
    final long shipments = shipmentRepository.count();
    final long pods = proofOfDeliveryRepository.count();

    LocalizedMessageDto stockError = new LocalizedMessageDto();
    stockError.setMessageKey(SOH_EXCEEDED);
    doThrow(new ExternalApiException(null, stockError))
        .when(stockEventService).submit(any(StockEventDto.class));

    String messageKey = restAssured.given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .body(transferSend())
        .when()
        .post(TRANSFER_SEND_URL)
        .then()
        .statusCode(400)
        .extract().path(MESSAGE_KEY);

    assertThat(messageKey, is(SOH_EXCEEDED));
    assertThat(orderRepository.count(), is(orders));
    assertThat(shipmentRepository.count(), is(shipments));
    assertThat(proofOfDeliveryRepository.count(), is(pods));
    assertThat(findPushedOrders(), hasSize(0));
  }

  private List<Order> findPushedOrders() {
    OrderSearchParams params = new OrderSearchParams();
    params.setSupplyingFacilityId(supplyingFacilityId);
    params.setType(Collections.singleton(OrderType.TRANSFER_PUSH.name()));
    return orderRepository.searchOrders(params, null, PAGE).getContent();
  }

  private List<Shipment> findShipments(Order order) {
    return shipmentRepository.findByOrder(order, PAGE).getContent();
  }

  private List<ProofOfDelivery> findProofsOfDelivery(Order order) {
    return proofOfDeliveryRepository.findAll()
        .stream()
        .filter(pod -> order.getId().equals(pod.getShipment().getOrder().getId()))
        .collect(Collectors.toList());
  }

  private void inTransaction(Runnable action) {
    new TransactionTemplate(transactionManager).execute(status -> {
      action.run();
      return null;
    });
  }

  private TransferSendDto transferSend() {
    VersionIdentityDto orderable = new VersionIdentityDto(UUID.randomUUID(), 1L);
    return new TransferSendDto(program.getId(), supplyingFacilityId, receivingFacilityId,
        "pushed", Arrays.asList(
            new TransferSendDto.LineItem(orderable, UUID.randomUUID(), 3L),
            new TransferSendDto.LineItem(orderable, UUID.randomUUID(), 4L)), null);
  }
}
