package org.openlmis.fulfillment.service.referencedata;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class RightDto {
  private UUID id;
  private String name;
  private RightType type;
  private String description;
  private Set<RightDto> attachments;
}
