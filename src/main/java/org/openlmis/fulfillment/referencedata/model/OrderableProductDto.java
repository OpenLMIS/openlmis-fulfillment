package org.openlmis.fulfillment.referencedata.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class OrderableProductDto {
  private UUID id;
  private String productCode;
  private String name;
  private long packSize;
  private long packRoundingThreshold;
  private boolean roundToZero;
  private Set<ProgramProductDto> programs;
  private DispensableDto dispensable;
}
