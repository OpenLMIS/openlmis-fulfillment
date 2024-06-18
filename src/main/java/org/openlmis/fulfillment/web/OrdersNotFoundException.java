package org.openlmis.fulfillment.web;

import java.util.Set;
import org.openlmis.fulfillment.i18n.MessageKeys;

public class OrdersNotFoundException extends NotFoundException {
  public OrdersNotFoundException(String messageKey, String... params) {
    super(messageKey, params);
  }

  public static OrdersNotFoundException newExceptionWithUuids(Set<String> uuids) {
    return new OrdersNotFoundException(MessageKeys.ORDER_NOT_FOUND_OR_WRONG_STATUS,
        uuids.toArray(new String[uuids.size()]));
  }
}
