package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;

public class MissingPermissionException extends AuthorizationException {

  public MissingPermissionException(String permissionName) {
    super(ERROR_PERMISSION_MISSING, permissionName);
  }

}
