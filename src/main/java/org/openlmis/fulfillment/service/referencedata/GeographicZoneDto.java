package org.openlmis.fulfillment.service.referencedata;

import lombok.Data;

import java.util.UUID;

@Data
public class GeographicZoneDto {
  private UUID id;
  private String code;
  private String name;
  private GeographicLevelDto level;
  private GeographicZoneDto parent;
}
