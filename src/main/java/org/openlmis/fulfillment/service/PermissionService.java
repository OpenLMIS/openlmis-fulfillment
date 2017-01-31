package org.openlmis.fulfillment.service;


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
  static final String REQUISITION_CONVERT_TO_ORDER = "REQUISITION_CONVERT_TO_ORDER";
  static final String FULFILLMENT_TRANSFER_ORDER = "FULFILLMENT_TRANSFER_ORDER";
  static final String PODS_MANAGE = "PODS_MANAGE";
  static final String ORDERS_VIEW = "ORDERS_VIEW";

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  /**
   * Checks if user has permission to create order.
   *
   * @param order checked if can be created by user.
   * @throws MissingPermissionException when used do not have permission.
   */
  public void canConvertToOrder(Order order) throws MissingPermissionException {
    hasPermission(REQUISITION_CONVERT_TO_ORDER, null, null,
        order.getSupplyingFacilityId());
  }

  public void canTransferOrder(Order order) throws MissingPermissionException {
    hasPermission(FULFILLMENT_TRANSFER_ORDER, null, null, order.getSupplyingFacilityId());
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
    hasPermission(PODS_MANAGE, null, null, proofOfDelivery.getOrder().getSupplyingFacilityId());
  }

  public void canViewOrder(Order order) throws MissingPermissionException {
    hasPermission(ORDERS_VIEW, null, null, order.getSupplyingFacilityId());
  }

  private void hasPermission(String rightName, UUID program, UUID facility, UUID warehouse)
      throws MissingPermissionException {
    UserDto user = authenticationHelper.getCurrentUser();
    RightDto right = authenticationHelper.getRight(rightName);
    ResultDto<Boolean> result = userReferenceDataService.hasRight(
        user.getId(), right.getId(), program, facility, warehouse
    );

    if (null == result || !result.getResult()) {
      throw new MissingPermissionException(rightName);
    }
  }
}
