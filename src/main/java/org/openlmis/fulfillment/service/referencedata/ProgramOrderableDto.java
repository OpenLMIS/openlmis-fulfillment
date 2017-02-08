package org.openlmis.fulfillment.service.referencedata;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProgramOrderableDto {
  private UUID programId;
  private UUID productId;
  private UUID orderableDisplayCategoryId;
  private String orderableCategoryDisplayName;
  private Integer orderableCategoryDisplayOrder;
  private Boolean active;
  private Boolean fullSupply;
  private Integer displayOrder;
  private Integer dosesPerPatient;
  private MoneyDto value;
}
