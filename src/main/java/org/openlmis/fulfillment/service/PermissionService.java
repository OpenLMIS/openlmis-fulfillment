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


import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.startsWith;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.referencedata.RightDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.MissingPermissionException;
import org.openlmis.fulfillment.web.shipment.ShipmentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@SuppressWarnings("PMD.TooManyMethods")
public class PermissionService {
  static final String ORDERS_TRANSFER = "ORDERS_TRANSFER";
  static final String PODS_MANAGE = "PODS_MANAGE";
  static final String ORDERS_VIEW = "ORDERS_VIEW";
  static final String ORDERS_EDIT = "ORDERS_EDIT";
  static final String SHIPMENTS_VIEW = "SHIPMENTS_VIEW";
  static final String SHIPMENTS_EDIT = "SHIPMENTS_EDIT";
  static final String SYSTEM_SETTINGS_MANAGE = "SYSTEM_SETTINGS_MANAGE";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Value("${auth.server.clientId}")
  private String serviceTokenClientId;

  @Value("${auth.server.clientId.apiKey.prefix}")
  private String apiKeyPrefix;

  public void canTransferOrder(Order order) {
    checkPermission(ORDERS_TRANSFER, order.getSupplyingFacilityId());
  }

  /**
   * Checks if user has permission to manage POD.
   *
   * @param proofOfDeliveryId ID of Proof of delivery
   */
  public void canManagePod(UUID proofOfDeliveryId) {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(proofOfDeliveryId);

    if (null == proofOfDelivery) {
      throw new MissingPermissionException(PODS_MANAGE);
    }

    canManagePod(proofOfDelivery);
  }

  public void canManagePod(ProofOfDelivery proofOfDelivery) {
    checkPermission(PODS_MANAGE, proofOfDelivery.getOrder().getSupplyingFacilityId());
  }

  public void canManageSystemSettings() {
    checkPermission(SYSTEM_SETTINGS_MANAGE, null);
  }

  public void canViewOrder(Order order) {
    canViewOrder(order.getSupplyingFacilityId());
  }

  public void canViewOrder(UUID supplyingFacility) {
    checkPermission(ORDERS_VIEW, supplyingFacility);
  }

  public void canEditOrder(Order order) {
    checkPermission(ORDERS_EDIT, order.getSupplyingFacilityId());
  }

  /**
   * Checks if user has permission to manage Shipments.
   *
   * @param shipment a shipment
   */
  public void canViewShipment(Shipment shipment) {
    checkPermission(SHIPMENTS_VIEW, shipment.getOrder().getSupplyingFacilityId());
  }

  /**
   * Checks if user has permission to edit Shipments.
   *
   * @param shipmentDto a shipment dto
   */
  public void canEditShipment(ShipmentDto shipmentDto) {
    UUID orderId = shipmentDto.getOrder().getId();
    Order order = orderRepository.findOne(orderId);
    if (order == null) {
      throw new MissingPermissionException(SHIPMENTS_EDIT);
    }
    checkPermission(SHIPMENTS_EDIT, order.getSupplyingFacilityId());
  }

  public boolean canViewOrderOrManagePod(Order order) {
    return hasPermission(ORDERS_VIEW, order.getSupplyingFacilityId())
        || hasPermission(PODS_MANAGE, order.getSupplyingFacilityId());
  }

  private boolean hasPermission(String rightName, UUID warehouse) {
    return hasPermission(rightName, warehouse, true, false);
  }

  private boolean hasPermission(String rightName, UUID warehouse, boolean allowUserTokens,
                                boolean allowApiKey) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder
        .getContext()
        .getAuthentication();

    return authentication.isClientOnly()
        ? checkServiceToken(allowApiKey, authentication)
        : checkUserToken(rightName, warehouse, allowUserTokens);
  }

  private boolean checkUserToken(String rightName, UUID warehouse,
                                 boolean allowUserTokens) {
    if (!allowUserTokens) {
      return false;
    }

    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result =  userReferenceDataService.hasRight(
        user.getId(), right.getId(), null, null, warehouse
    );

    return null != result && isTrue(result.getResult());
  }

  private boolean checkServiceToken(boolean allowApiKey,
                                    OAuth2Authentication authentication) {
    String clientId = authentication.getOAuth2Request().getClientId();

    if (serviceTokenClientId.equals(clientId)) {
      return true;
    }

    if (startsWith(clientId, apiKeyPrefix)) {
      return allowApiKey;
    }

    return false;
  }

  private void checkPermission(String rightName, UUID warehouse) {
    if (!hasPermission(rightName, warehouse)) {
      throw new MissingPermissionException(rightName);
    }
  }
}
