package org.openlmis.fulfillment.service;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.service.PermissionService.REQUISITION_CONVERT_TO_ORDER;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.service.referencedata.ResultDto;
import org.openlmis.fulfillment.service.referencedata.RightDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.MissingPermissionException;

import java.util.UUID;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @Mock
  private AuthenticationHelper authenticationHelper;

  @InjectMocks
  private PermissionService permissionService;

  @Mock
  private UserDto user;

  @Mock
  private RightDto requisitionConvertRight;

  private UUID userId = UUID.randomUUID();
  private UUID requisitionConvertRightId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private Order order =  new Order();

  @Before
  public void setUp() {

    order.setCreatedById(userId);
    order.setProgramId(programId);
    order.setSupplyingFacilityId(facilityId);

    when(user.getId()).thenReturn(userId);

    when(requisitionConvertRight.getId()).thenReturn(requisitionConvertRightId);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(authenticationHelper.getRight(REQUISITION_CONVERT_TO_ORDER)).thenReturn(
        requisitionConvertRight);
  }

  @Test
  public void canConvertToOrder() throws Exception {
    mockFulfillmentHasRight(requisitionConvertRightId, true);

    permissionService.canConvertToOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, REQUISITION_CONVERT_TO_ORDER, requisitionConvertRightId);
  }

  @Test
  public void cannotConvertToOrder() throws Exception {
    expectException(REQUISITION_CONVERT_TO_ORDER);

    permissionService.canConvertToOrder(order);
  }

  private void mockFulfillmentHasRight(UUID rightId, boolean assign) {
    ResultDto<Boolean> resultDto = new ResultDto<>(assign);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, facilityId)
    ).thenReturn(resultDto);
  }

  private void expectException(String rightName) {
    exception.expect(MissingPermissionException.class);
    exception.expectMessage(
        "You do not have the following permission to perform this action: " + rightName
    );
  }

  private void verifyFulfillmentRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null, facilityId);
  }

}

