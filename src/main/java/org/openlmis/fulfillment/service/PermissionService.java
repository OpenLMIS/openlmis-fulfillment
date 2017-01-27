package org.openlmis.fulfillment.service;


import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
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

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private AuthenticationHelper authenticationHelper;

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

  public void canManagePod(ProofOfDelivery proofOfDelivery) throws MissingPermissionException {
    hasPermission(PODS_MANAGE, null, null, proofOfDelivery.getOrder().getSupplyingFacilityId());
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
