package org.openlmis.fulfillment.service.referencedata;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RoleAssignmentDto {
  protected RoleDto role;
  protected UserDto user;
  private UUID id;
}
