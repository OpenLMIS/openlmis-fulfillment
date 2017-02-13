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

  /**
   * Tries to convert the string representation of <strong>status</strong> field to an instance of
   * {@link OrderStatus}.
   *
   * @return an instance of {@link OrderStatus} or {@code null} if the field is blank.
   * @throws ValidationException if the field contains a value that cannot be converted to enum.
   */
  @JsonIgnore
  OrderStatus getStatusAsEnum() {
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
