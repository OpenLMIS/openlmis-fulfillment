package org.openlmis.fulfillment.referencedata.model;

import lombok.Data;

import java.util.UUID;

@Data
public class GeographicLevelDto {
  private UUID id;
  private String code;
  private String name;
  private Integer levelNumber;
}
