package org.openlmis.fulfillment.service;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PERMISSION_MISSING;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_TRANSFER;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_EDIT;
import static org.openlmis.fulfillment.service.PermissionService.ORDERS_VIEW;
import static org.openlmis.fulfillment.service.PermissionService.PODS_MANAGE;

import com.google.common.collect.Lists;

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
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.service.referencedata.RightDto;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.referencedata.UserReferenceDataService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.MissingPermissionException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

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
  private RightDto fulfillmentOrdersEditRight;

  @Mock
  private RightDto fulfillmentTransferOrderRight;

  @Mock
  private RightDto fulfillmentManagePodRight;

  @Mock
  private RightDto fulfillmentOrdersViewRight;

  private UUID userId = UUID.randomUUID();
  private UUID fulfillmentTransferOrderRightId = UUID.randomUUID();
  private UUID fulfillmentManagePodRightId = UUID.randomUUID();
  private UUID fulfillmentOrdersViewRightId = UUID.randomUUID();
  private UUID fulfillmentOrdersEditRightId = UUID.randomUUID();
  private UUID programId = UUID.randomUUID();
  private UUID facilityId = UUID.randomUUID();
  private Order order =  new Order();
  private ProofOfDelivery proofOfDelivery;
  private SecurityContext securityContext;
  private OAuth2Authentication userClient;

  @Before
  public void setUp() {

    securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
    userClient = new OAuth2Authentication(mock(OAuth2Request.class), mock(Authentication.class));
    order.setCreatedById(userId);
    order.setProgramId(programId);
    order.setSupplyingFacilityId(facilityId);
    order.setOrderLineItems(Lists.newArrayList());

    proofOfDelivery = new ProofOfDelivery(order);

    when(user.getId()).thenReturn(userId);

    when(fulfillmentTransferOrderRight.getId()).thenReturn(fulfillmentTransferOrderRightId);
    when(fulfillmentManagePodRight.getId()).thenReturn(fulfillmentManagePodRightId);
    when(fulfillmentOrdersViewRight.getId()).thenReturn(fulfillmentOrdersViewRightId);
    when(fulfillmentOrdersEditRight.getId()).thenReturn(fulfillmentOrdersEditRightId);

    when(authenticationHelper.getCurrentUser()).thenReturn(user);

    when(authenticationHelper.getRight(ORDERS_TRANSFER)).thenReturn(
        fulfillmentTransferOrderRight);
    when(authenticationHelper.getRight(PODS_MANAGE)).thenReturn(
        fulfillmentManagePodRight);
    when(authenticationHelper.getRight(ORDERS_VIEW)).thenReturn(
        fulfillmentOrdersViewRight);
    when(authenticationHelper.getRight(ORDERS_EDIT)).thenReturn(
        fulfillmentOrdersEditRight);
  }

  @Test
  public void canTransferOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentTransferOrderRightId, true);

    permissionService.canTransferOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_TRANSFER, fulfillmentTransferOrderRightId);
  }

  @Test
  public void cannotTransferOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);
    expectException(ORDERS_TRANSFER);

    permissionService.canTransferOrder(order);
  }

  @Test
  public void canManagePod() throws Exception {
    mockFulfillmentHasRight(fulfillmentManagePodRightId, true);
    when(securityContext.getAuthentication()).thenReturn(userClient);


    permissionService.canManagePod(proofOfDelivery);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, PODS_MANAGE, fulfillmentManagePodRightId);
  }

  @Test
  public void cannotManagePod() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(PODS_MANAGE);

    permissionService.canManagePod(proofOfDelivery);
  }

  @Test
  public void canViewOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentOrdersViewRightId, true);

    permissionService.canViewOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_VIEW, fulfillmentOrdersViewRightId);
  }

  @Test
  public void cannotViewOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(ORDERS_VIEW);

    permissionService.canViewOrder(order);
  }

  @Test
  public void canEditOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    mockFulfillmentHasRight(fulfillmentOrdersEditRightId, true);

    permissionService.canEditOrder(order);

    InOrder order = inOrder(authenticationHelper, userReferenceDataService);
    verifyFulfillmentRight(order, ORDERS_EDIT, fulfillmentOrdersEditRightId);
  }

  @Test
  public void cannotEditOrder() throws Exception {
    when(securityContext.getAuthentication()).thenReturn(userClient);

    expectException(ORDERS_EDIT);

    permissionService.canEditOrder(order);
  }

  private void mockFulfillmentHasRight(UUID rightId, boolean assign) {
    ResultDto<Boolean> resultDto = new ResultDto<>(assign);
    when(userReferenceDataService
        .hasRight(userId, rightId, null, null, facilityId)
    ).thenReturn(resultDto);
  }

  private void expectException(String rightName) {
    exception.expect(MissingPermissionException.class);
    exception.expect(hasProperty("params", arrayContaining(rightName)));
    exception.expectMessage(ERROR_PERMISSION_MISSING);
  }

  private void verifyFulfillmentRight(InOrder order, String rightName, UUID rightId) {
    order.verify(authenticationHelper).getCurrentUser();
    order.verify(authenticationHelper).getRight(rightName);
    order.verify(userReferenceDataService).hasRight(userId, rightId, null, null, facilityId);
  }

}

