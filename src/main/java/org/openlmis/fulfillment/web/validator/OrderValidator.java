/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.fulfillment.web.validator;

import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class OrderValidator extends BaseValidator implements Validator {

  private static final String ORDER_LINE_ITEMS_FIELD = "orderLineItems";

  @Override
  public boolean supports(Class<?> clazz) {
    return OrderDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    OrderDto targetOrder = (OrderDto) target;

    boolean hasUniqueLineItems = targetOrder.getOrderLineItems().size()
        == targetOrder.getOrderLineItems().stream()
          .map(l -> l.getOrderableIdentity().getId())
          .distinct()
          .count();

    if (!hasUniqueLineItems) {
      errors.rejectValue(
          ORDER_LINE_ITEMS_FIELD,
          MessageKeys.ERROR_ORDERABLES_MUST_BE_UNIQUE
      );
    }

    for (int i = 0; i < targetOrder.getOrderLineItems().size(); i++) {
      OrderLineItem.Importer orderLineItem = targetOrder.getOrderLineItems().get(i);
      if (orderLineItem.getOrderedQuantity() == null) {
        errors.rejectValue("orderLineItems[" + i + "].orderedQuantity",
            MessageKeys.ERROR_ORDER_LINE_ITEMS_QUANTITY_REQUIRED);
      } else if (orderLineItem.getOrderedQuantity() < 0) {
        errors.rejectValue("orderLineItems[" + i + "].orderedQuantity",
            MessageKeys.ERROR_ORDER_LINE_ITEMS_QUANTITY_MUST_BE_POSITIVE);
      }
    }
  }
}
