package org.openlmis.fulfillment.service;

import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_ORDER_INVALID_STATUS;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.web.ValidationException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchParams {
  private static final ToEnum TO_ENUM = new ToEnum();

  UUID supplyingFacility;
  UUID requestingFacility;
  UUID program;
  UUID processingPeriod;
  Set<String> status;

  /**
   * Tries to convert the string representation of each status in the <strong>status</strong> field
   * to an instance of {@link OrderStatus}.
   *
   * @return a set with instances of {@link OrderStatus} or {@code null} if the field is empty.
   * @throws ValidationException if the field contains a value that cannot be converted to enum.
   */
  @JsonIgnore
  Set<OrderStatus> getStatusAsEnum() {
    return !isEmpty(status)
        ? status.stream().filter(StringUtils::isNotBlank).map(TO_ENUM).collect(Collectors.toSet())
        : null;
  }

  private static final class ToEnum implements Function<String, OrderStatus> {

    @Override
    public OrderStatus apply(String status) {
      OrderStatus orderStatus = OrderStatus.fromString(status);

      if (null == orderStatus) {
        throw new ValidationException(ERROR_ORDER_INVALID_STATUS, status);
      }

      return orderStatus;
    }
  }
}
