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

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_NOT_FOUND;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_TRANSFER;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.SHIPMENTS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.SYSTEM_SETTINGS_MANAGE;
import static org.openlmis.fulfillment.testutils.OAuth2AuthenticationDataBuilder.API_KEY_PREFIX;
import static org.openlmis.fulfillment.testutils.OAuth2AuthenticationDataBuilder.SERVICE_CLIENT_ID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.ProofOfDeliveryDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.referencedata.RightDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.testutils.OAuth2AuthenticationDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.MissingPermissionException;
import org.openlmis.fulfillment.web.ValidationException;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.openlmis.fulfillment.web.shipment.ShipmentDtoDataBuilder;
import org.openlmis.fulfillment.web.util.ObjectReferenceDto;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private ShipmentRepository shipmentRepository;

  @InjectMocks
  private PermissionService permissionService;

  @Mock
  private UserDto user;

  @Mock
  private RightDto fulfillmentOrdersEditRight;

  @Mock
  private RightDto fulfillmentTransferOrderRight;

  @Mock
  private RightDto fulfillmentManagePodRight;

  @Mock
  private RightDto shipmentsViewRight;

  @Mock
  private RightDto shipmentsEditRight;

  @Mock
  private RightDto fulfillmentOrdersViewRight;

  @Mock
  private RightDto systemSettingsManageRight;

  private UUID fulfillmentTransferOrderRightId = UUID.randomUUID();
  private UUID fulfillmentManagePodRightId = UUID.randomUUID();
  private UUID fulfillmentOrdersViewRightId = UUID.randomUUID();
  private UUID fulfillmentOrdersEditRightId = UUID.randomUUID();
  private UUID shipmentsViewRightId = UUID.randomUUID();
  private UUID shipmentsEditRightId = UUID.randomUUID();
  private UUID systemSettingsManageRightId = UUID.randomUUID();

  private UUID userId;
  private UUID facilityId;

  private ProofOfDelivery proofOfDelivery;
  private Shipment shipment;
  private Order order;

  private ShipmentDto shipmentDto;

  private SecurityContext securityContext;
  private OAuth2Authentication userClient;
  private OAuth2Authentication trustedClient;
  private OAuth2Authentication apiKeyClient;

  @Before
  public void setUp() {
    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);

    trustedClient = new OAuth2AuthenticationDataBuilder().buildServiceAuthentication();
    userClient = new OAuth2AuthenticationDataBuilder().buildUserAuthentication();
    apiKeyClient = new OAuth2AuthenticationDataBuilder().buildApiKeyAuthentication();

    proofOfDelivery = new ProofOfDeliveryDataBuilder().build();
    shipment = proofOfDelivery.getShipment();
    order = shipment.getOrder();

    userId = order.getCreatedById();
    facilityId = order.getSupplyingFacilityId();

    shipmentDto = new ShipmentDtoDataBuilder()
        .withOrder(new ObjectReferenceDto(order.getId()))
        .build();

    when(orderRepository.findOne(order.getId())).thenReturn(order);
    when(shipmentRepository.findOne(shipment.getId())).thenReturn(shipment);
    when(user.getId()).thenReturn(userId);

    mockRight(fulfillmentTransferOrderRight, fulfillmentTransferOrderRightId, ORDERS_TRANSFER);
    mockRight(fulfillmentManagePodRight, fulfillmentManagePodRightId, PODS_MANAGE);
    mockRight(fulfillmentOrdersViewRight, fulfillmentOrdersViewRightId, ORDERS_VIEW);
    mockRight(fulfillmentOrdersEditRight, fulfillmentOrdersEditRightId, ORDERS_EDIT);
    mockRight(systemSettingsManageRight, systemSettingsManageRightId, SYSTEM_SETTINGS_MANAGE);
    mockRight(shipmentsEditRight, shipmentsEditRightId, SHIPMENTS_EDIT);
    mockRight(shipmentsViewRight, shipmentsViewRightId, SHIPMENTS_VIEW);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);
    when(securityContext.getAuthentication()).thenReturn(userClient);

    ReflectionTestUtils.setField(permissionService, "serviceTokenClientId", SERVICE_CLIENT_ID);
    ReflectionTestUtils.setField(permissionService, "apiKeyPrefix", API_KEY_PREFIX);
  }

  @Test
  public void canTransferOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentTransferOrderRightId, true, facilityId);

    permissionService.canTransferOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_TRANSFER, fulfillmentTransferOrderRightId, facilityId);
  }

  @Test
  public void cannotTransferOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    expectException(ORDERS_TRANSFER);

    permissionService.canTransferOrder(order);
  }

  @Test
  public void canManageSystemSettingsByServiceToken() {
    when(securityContext.getAuthentication()).thenReturn(trustedClient);
    //If endpoint does not allow for service level token authorization, method will throw Exception.
    permissionService.canManageSystemSettings();

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);

    order.verify(authenticationHelper, never()).getCurrentUser();
    order.verify(authenticationHelper, never()).getRight(SYSTEM_SETTINGS_MANAGE);
    order.verify(userReferenceDataService, never()).hasRight(userId, systemSettingsManageRightId,
        null, null, null);
  }

  @Test
  public void cannotManageSystemSettingsByApiKeyToken() {
    when(securityContext.getAuthentication()).thenReturn(apiKeyClient);
    expectException(SYSTEM_SETTINGS_MANAGE);

    //If endpoint does not allow for service level token authorization, method will throw Exception.
    permissionService.canManageSystemSettings();
  }

  @Test
  public void canManageSystemSettingsByUserToken() {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(systemSettingsManageRightId, true, null);

    permissionService.canManageSystemSettings();

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, SYSTEM_SETTINGS_MANAGE, systemSettingsManageRightId, null);
  }

  @Test
  public void cannotManageSystemSettingsByUserToken() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    expectException(SYSTEM_SETTINGS_MANAGE);

    permissionService.canManageSystemSettings();
  }

  @Test
  public void canManagePod() throws Exception {
    mockFulfillmentHasRight(fulfillmentManagePodRightId, true, facilityId);
    when(securityContext.getAuthentication()).thenReturn(userClient);


    permissionService.canManagePod(proofOfDelivery);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, PODS_MANAGE, fulfillmentManagePodRightId, facilityId);
  }

  @Test
  public void cannotManagePod() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(PODS_MANAGE);

    permissionService.canManagePod(proofOfDelivery);
  }

  @Test
  public void canViewOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentOrdersViewRightId, true, facilityId);

    permissionService.canViewOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_VIEW, fulfillmentOrdersViewRightId, facilityId);
  }

  @Test
  public void cannotViewOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(ORDERS_VIEW);

    permissionService.canViewOrder(order);
  }

  @Test
  public void canEditOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentOrdersEditRightId, true, facilityId);

    permissionService.canEditOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_EDIT, fulfillmentOrdersEditRightId, facilityId);
  }

  @Test
  public void cannotEditOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(ORDERS_EDIT);

    permissionService.canEditOrder(order);
  }

  @Test
  public void canManageShipment() throws Exception {
    mockFulfillmentHasRight(shipmentsEditRightId, true, facilityId);

    permissionService.canEditShipment(shipmentDto);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, SHIPMENTS_EDIT, shipmentsEditRightId, facilityId);
  }

  @Test
  public void cannotManageShipmentWhenUserHasNoRights() throws Exception {
    expectException(SHIPMENTS_EDIT);

    permissionService.canEditShipment(shipmentDto);
  }

  @Test
  public void cannotManageShipmentWhenOrderIsNotFound() throws Exception {
    mockFulfillmentHasRight(fulfillmentOrdersEditRightId, true, facilityId);
    when(orderRepository.findOne(order.getId())).thenReturn(null);
    exception.expect(ValidationException.class);
    exception.expect(hasProperty("params", arrayContaining(order.getId().toString())));
    exception.expectMessage(ERROR_ORDER_NOT_FOUND);

    permissionService.canEditShipment(shipmentDto);
  }

  @Test
  public void canViewShipment() throws Exception {
    mockFulfillmentHasRight(shipmentsViewRightId, true, facilityId);

    permissionService.canViewShipment(shipment);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, SHIPMENTS_VIEW, shipmentsViewRightId, facilityId);
  }

  @Test
  public void cannotViewShipmentWhenUserHasNoRights() throws Exception {
    expectException(SHIPMENTS_VIEW);

    permissionService.canViewShipment(shipment);
  }

  private void mockRight(RightDto right, UUID rightId, String rightName) {
    when(authenticationHelper.getRight(rightName))
        .thenReturn(right);
    when(right.getId()).thenReturn(rightId, rightId);
  }

  private void mockFulfillmentHasRight(UUID rightId, boolean assign, UUID facility) {
    ResultDto<Boolean> resultDto = new ResultDto<>(assign);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, facility)
    ).thenReturn(resultDto);
  }

  private void expectException(String rightName) {
    exception.expect(MissingPermissionException.class);
    exception.expect(hasProperty("params", arrayContaining(rightName)));
    exception.expectMessage(ERROR_PERMISSION_MISSING);
  }

  private void verifyFulfillmentRight(InOrder order, String rightName, UUID rightId,
                                      UUID facility) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null, facility);
  }

}

