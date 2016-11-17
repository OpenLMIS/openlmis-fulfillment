package org.openlmis.order.referencedata.model;

import lombok.Data;

import java.util.UUID;

@Data
public class FacilityOperatorDto {
  private UUID id;
  private String code;
  private String name;
}
