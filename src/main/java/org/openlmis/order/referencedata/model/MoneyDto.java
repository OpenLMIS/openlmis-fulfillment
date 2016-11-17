package org.openlmis.order.referencedata.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MoneyDto {
  private BigDecimal value;
}
