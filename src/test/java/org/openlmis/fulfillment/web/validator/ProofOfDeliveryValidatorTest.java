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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.ProofOfDeliveryDataBuilder;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.testutils.OrderableDataBuilder;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDtoBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@RunWith(MockitoJUnitRunner.class)
public class ProofOfDeliveryValidatorTest {
  private static final String ENV_INJECT_FIELD = "allowEmptyShipment";
  private static final String POD_OBJECT = "proofOfDelivery";
  private static final String POD_LINE_ITEMS = "lineItems";

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;
  @InjectMocks
  private ProofOfDeliveryDtoBuilder proofOfDeliveryDtoBuilder;
  @InjectMocks
  private ProofOfDeliveryValidator proofOfDeliveryValidator;

  @Before
  public void setup() {
    when(orderableReferenceDataService.findByIdentities(anySet())).thenAnswer(
        invocation -> ((Set<?>) invocation.getArgument(0)).stream()
            .map(ignore -> new OrderableDataBuilder().build()).collect(toList()));
  }

  @Test
  public void shouldThrowErrorForEmptyLinesByDefault() {
    final ProofOfDeliveryDto emptyProofOfDelivery = proofOfDeliveryDtoBuilder
        .build(new ProofOfDeliveryDataBuilder().withLineItems(emptyList()).build());
    final Errors errors = new MapBindingResult(new HashMap<>(), POD_OBJECT);

    proofOfDeliveryValidator.validate(emptyProofOfDelivery, errors);

    assertTrue("Should mark `lineItems` with error.", errors.hasFieldErrors(POD_LINE_ITEMS));
  }

  @Test
  public void shouldNotThrowErrorForEmptyLinesWhenFlagIsEnabled() {
    // This test is not intended to test Spring's @Value injection
    ReflectionTestUtils.setField(proofOfDeliveryValidator, ENV_INJECT_FIELD, true);

    final ProofOfDeliveryDto emptyProofOfDelivery = proofOfDeliveryDtoBuilder
        .build(new ProofOfDeliveryDataBuilder().withLineItems(emptyList()).build());
    final Errors errors = new MapBindingResult(new HashMap<>(), POD_OBJECT);

    proofOfDeliveryValidator.validate(emptyProofOfDelivery, errors);

    assertFalse("Should NOT mark `lineItems` with error.", errors.hasFieldErrors(POD_LINE_ITEMS));
  }
}
