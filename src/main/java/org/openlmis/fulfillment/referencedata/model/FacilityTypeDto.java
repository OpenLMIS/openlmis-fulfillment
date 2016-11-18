package org.openlmis.fulfillment.referencedata.model;

import lombok.Data;

import java.util.UUID;

@Data
public class FacilityTypeDto {
  private UUID id;
  private String code;
  private String name;
  private String description;
  private Integer displayOrder;
  private Boolean active;
}
