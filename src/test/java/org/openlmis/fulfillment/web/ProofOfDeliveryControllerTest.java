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

package org.openlmis.fulfillment.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.validator.ProofOfDeliveryValidator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@RunWith(MockitoJUnitRunner.class)
public class ProofOfDeliveryControllerTest {
  private static final String POD_OBJECT = "proofOfDelivery";
  private static final String TEST_FIELD = "testField";
  private static final String TEST_ERROR_MESSAGE = "This is error message from validator.";

  @Mock
  private ProofOfDeliveryValidator proofOfDeliveryValidator;

  @InjectMocks
  private ProofOfDeliveryController controller;

  @Test
  public void updateProofOfDeliveryShouldPassErrorFromValidator() {
    final UUID dummyUuid = UUID.fromString("697bc284-77be-4ab1-9c4e-32b86e7d80b3");
    final ProofOfDeliveryDto dummyProofOfDelivery = new ProofOfDeliveryDto();
    final OAuth2Authentication dummyAuthentication = mock(OAuth2Authentication.class);

    doAnswer(invocation -> {
      ((Errors) invocation.getArgument(1)).rejectValue(TEST_FIELD, TEST_ERROR_MESSAGE);
      return null;
    }).when(proofOfDeliveryValidator).validate(any(), any(Errors.class));

    try {
      final BindingResult bindingResult = new MapBindingResult(new HashMap<>(), POD_OBJECT);
      controller.updateProofOfDelivery(dummyUuid, dummyProofOfDelivery, bindingResult,
          dummyAuthentication);
      fail("Should throw ValidationException");
    } catch (ValidationException expected) {
      assertNotNull(expected.getMessage());
      assertTrue(expected.getMessage().contains(TEST_ERROR_MESSAGE));
    }
  }
}
