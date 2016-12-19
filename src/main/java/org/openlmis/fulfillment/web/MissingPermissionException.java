package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_MISSING_PERMISSION;

public class MissingPermissionException extends AuthorizationException {

  public MissingPermissionException(String permissionName) {
    super(ERROR_MISSING_PERMISSION, permissionName);
  }

}
