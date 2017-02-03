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
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {
  static final String FULFILLMENT_TRANSFER_ORDER = "FULFILLMENT_TRANSFER_ORDER";
  static final String PODS_MANAGE = "PODS_MANAGE";
  static final String ORDERS_VIEW = "ORDERS_VIEW";
  static final String ORDERS_EDIT = "ORDERS_EDIT";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  public void canTransferOrder(Order order) throws MissingPermissionException {
    throwIfMissingPermission(FULFILLMENT_TRANSFER_ORDER, order.getSupplyingFacilityId());
  }

  /**
   * Checks if user has permission to manage POD.
   *
   * @param proofOfDeliveryId ID of Proof of delivery
   * @throws MissingPermissionException when used do not have permission.
   */
  public void canManagePod(UUID proofOfDeliveryId) throws MissingPermissionException {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(proofOfDeliveryId);

    if (null == proofOfDelivery) {
      throw new MissingPermissionException(PODS_MANAGE);
    }

    canManagePod(proofOfDelivery);
  }

  public void canManagePod(ProofOfDelivery proofOfDelivery) throws MissingPermissionException {
    throwIfMissingPermission(PODS_MANAGE, proofOfDelivery.getOrder().getSupplyingFacilityId());
  }

  public void canViewOrder(Order order) throws MissingPermissionException {
    canViewOrder(order.getSupplyingFacilityId());
  }

  public void canViewOrder(UUID supplyingFacility) throws MissingPermissionException {
    throwIfMissingPermission(ORDERS_VIEW, supplyingFacility);
  }

  public void canEditOrder(Order order) throws MissingPermissionException {
    throwIfMissingPermission(ORDERS_EDIT, order.getSupplyingFacilityId());
  }

  public boolean checkIfCanViewOrder(Order order) {
    return hasPermission(ORDERS_VIEW, order.getSupplyingFacilityId());
  }

  private boolean hasPermission(String rightName, UUID warehouse) {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result =  userReferenceDataService.hasRight(
        user.getId(), right.getId(), null, null, warehouse
    );

    return null != result && isTrue(result.getResult());
  }

  private void throwIfMissingPermission(String rightName, UUID warehouse)
      throws MissingPermissionException {
    if (!hasPermission(rightName, warehouse)) {
      throw new MissingPermissionException(rightName);
    }
  }
}
