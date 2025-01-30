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

import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryStatus;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.FulfillmentNotificationService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.OrderableReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.stockmanagement.StockEventStockManagementService;
import org.openlmis.fulfillment.testutils.ShipmentDataBuilder;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.stockmanagement.StockEventDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDtoBuilder;
import org.openlmis.fulfillment.web.util.StockEventBuilder;
import org.openlmis.fulfillment.web.validator.ProofOfDeliveryValidator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

@SuppressWarnings("PMD.UnusedPrivateField")
@RunWith(MockitoJUnitRunner.class)
public class ProofOfDeliveryControllerTest {
  private static final String POD_OBJECT = "proofOfDelivery";
  private static final String TEST_FIELD = "testField";
  private static final String TEST_ERROR_MESSAGE = "This is error message from validator.";
  private static final String TEST_PERSON_NAME = "testPerson";

  @Mock
  private ProofOfDeliveryValidator proofOfDeliveryValidator;

  @Mock
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Mock
  private OrderableReferenceDataService orderableReferenceDataService;

  @Mock
  private PermissionService permissionService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @Mock
  private DateHelper dateHelper;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private StockEventBuilder stockEventBuilder;

  @Mock
  private StockEventStockManagementService stockEventStockManagementService;

  @Mock
  private FulfillmentNotificationService fulfillmentNotificationService;

  @Mock
  private ProofOfDeliveryDtoBuilder dtoBuilder;

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

  @Test
  public void updateProofOfDeliveryShouldUpdateAndSubmitStockEvent() {
    final UUID podId = UUID.fromString("697bc284-77be-4ab1-9c4e-32b86e7d80b4");

    final Shipment shipment = new ShipmentDataBuilder().build();
    final ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
    proofOfDeliveryDto.setId(podId);
    proofOfDeliveryDto.setShipment(shipment);
    proofOfDeliveryDto.setStatus(ProofOfDeliveryStatus.CONFIRMED);
    proofOfDeliveryDto.setLineItems(emptyList());
    proofOfDeliveryDto.setDeliveredBy(TEST_PERSON_NAME);
    proofOfDeliveryDto.setReceivedBy(TEST_PERSON_NAME);
    proofOfDeliveryDto.setReceivedDate(LocalDate.now());

    final ProofOfDelivery proofOfDelivery =
        new ProofOfDelivery(shipment, ProofOfDeliveryStatus.INITIATED, emptyList(), null, null,
            null);

    final BindingResult dummyBindingResult = new MapBindingResult(new HashMap<>(), POD_OBJECT);
    final OAuth2Authentication dummyAuthentication = mock(OAuth2Authentication.class);

    final StockEventDto stockEventDto = new StockEventDto();

    when(proofOfDeliveryRepository.findById(podId)).thenReturn(of(proofOfDelivery));
    when(authenticationHelper.getCurrentUser()).thenReturn(mock(UserDto.class));
    when(dateHelper.getCurrentDateTimeWithSystemZone()).thenReturn(now());
    when(stockEventBuilder.fromProofOfDelivery(proofOfDelivery)).thenReturn(of(stockEventDto));

    controller
        .updateProofOfDelivery(proofOfDeliveryDto.getId(), proofOfDeliveryDto, dummyBindingResult,
            dummyAuthentication);

    verify(proofOfDeliveryRepository).save(proofOfDelivery);
    verify(stockEventStockManagementService).submit(stockEventDto);
  }
}
