package org.openlmis.fulfillment.service;


import static org.apache.commons.lang.BooleanUtils.isTrue;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.referencedata.RightDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.MissingPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {
  static final String ORDERS_TRANSFER = "ORDERS_TRANSFER";
  static final String PODS_MANAGE = "PODS_MANAGE";
  static final String ORDERS_VIEW = "ORDERS_VIEW";
  static final String ORDERS_EDIT = "ORDERS_EDIT";
  static final String SYSTEM_SETTINGS_MANAGE = "SYSTEM_SETTINGS_MANAGE";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  public void canTransferOrder(Order order) {
    checkPermission(ORDERS_TRANSFER, order.getSupplyingFacilityId(), false);
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
    checkPermission(PODS_MANAGE, proofOfDelivery.getOrder().getSupplyingFacilityId(),
        false);
  }

  public void canManageSystemSettings() {
    checkPermission(SYSTEM_SETTINGS_MANAGE, null, true);
  }

  public void canViewOrder(Order order) {
    canViewOrder(order.getSupplyingFacilityId());
  }

  public void canViewOrder(UUID supplyingFacility) {
    checkPermission(ORDERS_VIEW, supplyingFacility, true);
  }

  public void canEditOrder(Order order) {
    checkPermission(ORDERS_EDIT, order.getSupplyingFacilityId(), false);
  }

  public boolean canViewOrderOrManagePod(Order order) {
    return hasPermission(ORDERS_VIEW, order.getSupplyingFacilityId(), true)
        || hasPermission(PODS_MANAGE, order.getSupplyingFacilityId(), false);
  }

  private boolean hasPermission(String rightName, UUID warehouse, boolean allowServiceTokens) {
    OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
        .getAuthentication();
    if (authentication.isClientOnly()) {
      return allowServiceTokens;
    }
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result =  userReferenceDataService.hasRight(
        user.getId(), right.getId(), null, null, warehouse
    );

    return null != result && isTrue(result.getResult());
  }

  private void checkPermission(String rightName, UUID warehouse, boolean
      allowServiceTokens) {
    if (!hasPermission(rightName, warehouse, allowServiceTokens)) {
      throw new MissingPermissionException(rightName);
    }
  }
}
