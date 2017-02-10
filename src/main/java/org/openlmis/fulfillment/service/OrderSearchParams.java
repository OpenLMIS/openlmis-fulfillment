package org.openlmis.fulfillment.service;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.web.ValidationException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchParams {
  UUID supplyingFacility;
  UUID requestingFacility;
  UUID program;
  UUID processingPeriod;
  String status;

  @JsonIgnore
  OrderStatus convertStatus() {
    OrderStatus orderStatus = null;
    
    if (isNotBlank(status)) {
      orderStatus = OrderStatus.fromString(status);

      if (null == orderStatus) {
        throw new ValidationException(ERROR_ORDER_INVALID_STATUS, status);
      }
    }

    return orderStatus;
  }
}
