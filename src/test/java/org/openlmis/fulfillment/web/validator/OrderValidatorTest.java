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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.fulfillment.OrderDataBuilder;
import org.openlmis.fulfillment.OrderLineItemDataBuilder;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.referencedata.OrderableDto;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderDtoBuilder;
import org.openlmis.fulfillment.web.util.OrderExportHelper;
import org.openlmis.fulfillment.web.util.OrderLineItemDto;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

@SuppressWarnings("unused")
public class OrderValidatorTest {

  private static final String ORDER_DTO = "orderDto";

  @Rule
  public MockitoRule mockito = MockitoJUnit.rule();

  private final OrderValidator validator = new OrderValidator();

  @InjectMocks
  private OrderLineItemDataBuilder orderLineItemDataBuilder;

  @InjectMocks
  private OrderDataBuilder orderDataBuilder;

  @InjectMocks
  private OrderDtoBuilder orderDtoBuilder = new OrderDtoBuilder();

  @Mock
  // INFO: Field used by orderDtoBuilder
  private OrderExportHelper orderExportHelper;

  @InjectMocks
  private OrderableDataBuilder orderableDataBuilder;

  private final UUID orderableIdA = UUID.fromString("d602d0c6-4052-456c-8ccd-61b4ad77bece");
  private final UUID orderableIdB = UUID.fromString("c9e65f02-f84f-4ba2-85f7-e2cb6f0989af");

  @Test
  public void shouldSupportOrderDto() {
    assertTrue(validator.supports(OrderDto.class));
  }

  @Test
  public void shouldNotFailValidationForUniqueOrderables() {
    OrderDto orderDto = buildOrderDtoWithOrderableIdsAndQuantity(
        Arrays.asList(orderableIdA, orderableIdB), 1L
    );

    Errors errors = new DirectFieldBindingResult(orderDto, ORDER_DTO);

    validator.validate(orderDto, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  public void shouldFailValidationForNonUniqueOrderables() {
    OrderDto orderDto = buildOrderDtoWithOrderableIdsAndQuantity(
        Arrays.asList(orderableIdA, orderableIdA), 1L
    );

    Errors errors = new DirectFieldBindingResult(orderDto, ORDER_DTO);

    validator.validate(orderDto, errors);

    assertTrue(errors.hasErrors());
  }

  @Test
  public void shouldFailValidationForNonUniqueOrderablesWithProperMessageKey() {
    OrderDto orderDto = buildOrderDtoWithOrderableIdsAndQuantity(
        Arrays.asList(orderableIdA, orderableIdA), 1L
    );

    Errors errors = new DirectFieldBindingResult(orderDto, ORDER_DTO);

    validator.validate(orderDto, errors);

    FieldError fieldError = errors.getFieldError();

    assertTrue(errors.hasErrors());
    assertNotNull(fieldError);
    assertEquals(MessageKeys.ERROR_ORDERABLES_MUST_BE_UNIQUE, fieldError.getCode());
  }

  @Test
  public void shouldFailValidationWhenOrderItemQuantityIsNull() {
    OrderDto orderDto = buildOrderDtoWithOrderableIdsAndQuantity(
        Collections.singletonList(orderableIdA), null
    );

    Errors errors = new DirectFieldBindingResult(orderDto, ORDER_DTO);

    validator.validateItemsQuantity(orderDto, errors);

    FieldError fieldError = errors.getFieldError();

    assertTrue(errors.hasErrors());
    assertNotNull(fieldError);
    assertEquals(MessageKeys.ERROR_ORDER_LINE_ITEMS_QUANTITY_REQUIRED, fieldError.getCode());
  }

  @Test
  public void shouldFailValidationWhenOrderItemQuantityIsNegative() {
    OrderDto orderDto = buildOrderDtoWithOrderableIdsAndQuantity(
        Collections.singletonList(orderableIdA), -1L
    );

    Errors errors = new DirectFieldBindingResult(orderDto, ORDER_DTO);

    validator.validateItemsQuantity(orderDto, errors);

    FieldError fieldError = errors.getFieldError();

    assertTrue(errors.hasErrors());
    assertNotNull(fieldError);
    assertEquals(MessageKeys.ERROR_ORDER_LINE_ITEMS_QUANTITY_MUST_BE_POSITIVE,
        fieldError.getCode());
  }

  private OrderDto buildOrderDtoWithOrderableIdsAndQuantity(List<UUID> ids, Long quantity) {
    Order order = orderDataBuilder.withLineItems(ids.stream()
        .map(id -> orderLineItemDataBuilder.withOrderable(id, 1L).build())
        .collect(Collectors.toList())
    ).build();

    OrderDto orderDto = orderDtoBuilder.build(order);

    List<OrderLineItemDto> orderLineItemDtos = new ArrayList<>();

    ExporterBuilder exporterBuilder = new ExporterBuilder();

    List<OrderableDto> orderableDtos = ids.stream()
        .map(id -> orderableDataBuilder.withId(id)
            .withVersionNumber(1L)
            .build()
        ).collect(Collectors.toList());

    OrderLineItemDto orderLineItemDto;

    for (OrderLineItem lineItem : order.getOrderLineItems()) {
      orderLineItemDto = new OrderLineItemDto();
      exporterBuilder.export(lineItem, orderLineItemDto, orderableDtos);
      orderLineItemDto.setOrderedQuantity(quantity);
      orderLineItemDtos.add(orderLineItemDto);
    }

    orderDto.setOrderLineItems(orderLineItemDtos);

    return orderDto;
  }
}
