package org.openlmis.fulfillment.service.referencedata;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SupervisoryNodeDto {
  private UUID id;
  private String code;
  private String name;
  private String description;
  private FacilityDto facility;
  private SupervisoryNodeDto parentNode;
  private Set<SupervisoryNodeDto> childNodes;
}
