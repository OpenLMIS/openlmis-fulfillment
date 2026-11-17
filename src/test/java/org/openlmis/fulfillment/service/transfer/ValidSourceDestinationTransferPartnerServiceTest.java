/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2026 VillageReach
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

package org.openlmis.fulfillment.service.transfer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourcesStockManagementService;
import org.openlmis.fulfillment.testutils.FacilityDataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class ValidSourceDestinationTransferPartnerServiceTest {

  @Mock
  private ValidSourcesStockManagementService validSourcesService;

  @Mock
  private ValidDestinationsStockManagementService validDestinationsService;

  @Mock
  private FacilityReferenceDataService facilityReferenceDataService;

  @InjectMocks
  private ValidSourceDestinationTransferPartnerService service;

  private final UUID programId = UUID.randomUUID();
  private final UUID sender = UUID.randomUUID();
  private final UUID receiver = UUID.randomUUID();
  private final UUID other = UUID.randomUUID();

  @Test
  public void shouldAcceptPairWhenBothRowsExist() {
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Collections.singleton(receiver));
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Collections.singleton(sender));

    assertThat(service.isValidPair(sender, receiver, programId), is(true));
  }

  @Test
  public void shouldRejectPairWhenOnlyDestinationExists() {
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Collections.singleton(receiver));
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Collections.singleton(other));

    assertThat(service.isValidPair(sender, receiver, programId), is(false));
  }

  @Test
  public void shouldRejectPairWhenDestinationIsMissingWithoutCheckingSources() {
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Collections.emptySet());

    assertThat(service.isValidPair(sender, receiver, programId), is(false));
    verify(validSourcesService, never()).findFacilityIds(any(), any());
  }

  @Test
  public void shouldRejectSameFacilityAndMissingIds() {
    assertThat(service.isValidPair(sender, sender, programId), is(false));
    assertThat(service.isValidPair(null, receiver, programId), is(false));
    assertThat(service.isValidPair(sender, null, programId), is(false));
    assertThat(service.isValidPair(sender, receiver, null), is(false));

    verifyZeroInteractions(validSourcesService, validDestinationsService);
  }

  @Test
  public void shouldReturnSendPartnersThatAcceptTheFacilityAsSource() {
    // receiver accepts sender as a source, other does not; sender itself is never a candidate
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Sets.newHashSet(receiver, other, sender));
    FacilityDto receiverFacility = new FacilityDataBuilder().withId(receiver).build();
    FacilityDto otherFacility = new FacilityDataBuilder().withId(other).build();
    when(facilityReferenceDataService.findByIds(Sets.newHashSet(receiver, other)))
        .thenReturn(Arrays.asList(receiverFacility, otherFacility));
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Collections.singleton(sender));
    when(validSourcesService.findFacilityIds(programId, other))
        .thenReturn(Collections.emptySet());

    List<FacilityDto> partners = service.getPartners(sender, programId, TransferDirection.SEND);

    assertThat(partners, contains(receiverFacility));
    verify(validSourcesService, never()).findFacilityIds(programId, sender);
  }

  @Test
  public void shouldReturnRequestPartnersThatAcceptTheFacilityAsDestination() {
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Sets.newHashSet(sender, other));
    FacilityDto senderFacility = new FacilityDataBuilder().withId(sender).build();
    FacilityDto otherFacility = new FacilityDataBuilder().withId(other).build();
    when(facilityReferenceDataService.findByIds(Sets.newHashSet(sender, other)))
        .thenReturn(Arrays.asList(senderFacility, otherFacility));
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Collections.singleton(receiver));
    when(validDestinationsService.findFacilityIds(programId, other))
        .thenReturn(Collections.singleton(UUID.randomUUID()));

    List<FacilityDto> partners =
        service.getPartners(receiver, programId, TransferDirection.REQUEST);

    assertThat(partners, contains(senderFacility));
  }

  @Test
  public void shouldAskStockManagementOnceForCandidatesWithSameTypeAndZone() {
    FacilityDto first = new FacilityDataBuilder().withId(receiver).build();
    FacilityDto second = new FacilityDataBuilder().withId(other)
        .withType(first.getType()).withGeographicZone(first.getGeographicZone()).build();
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Sets.newHashSet(receiver, other));
    when(facilityReferenceDataService.findByIds(Sets.newHashSet(receiver, other)))
        .thenReturn(Arrays.asList(first, second));
    // whichever of the two is asked, the answer covers both
    when(validSourcesService.findFacilityIds(eq(programId), any(UUID.class)))
        .thenReturn(Collections.singleton(sender));

    List<FacilityDto> partners = service.getPartners(sender, programId, TransferDirection.SEND);

    assertThat(partners, containsInAnyOrder(first, second));
    verify(validSourcesService, times(1)).findFacilityIds(eq(programId), any(UUID.class));
  }

  @Test
  public void shouldAskStockManagementSeparatelyForDifferentZones() {
    // same type, different zones: affinity may give them different answers
    FacilityDto first = new FacilityDataBuilder().withId(receiver).build();
    FacilityDto second = new FacilityDataBuilder().withId(other).withType(first.getType()).build();
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Sets.newHashSet(receiver, other));
    when(facilityReferenceDataService.findByIds(Sets.newHashSet(receiver, other)))
        .thenReturn(Arrays.asList(first, second));
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Collections.singleton(sender));
    when(validSourcesService.findFacilityIds(programId, other))
        .thenReturn(Collections.emptySet());

    assertThat(service.getPartners(sender, programId, TransferDirection.SEND), contains(first));
  }

  @Test
  public void shouldSortPartnersByName() {
    FacilityDto zulu = new FacilityDataBuilder().withId(receiver).withName("zulu").build();
    FacilityDto alpha = new FacilityDataBuilder().withId(other).withName("Alpha").build();
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Sets.newHashSet(receiver, other));
    when(facilityReferenceDataService.findByIds(anyCollection()))
        .thenReturn(Arrays.asList(zulu, alpha));
    when(validSourcesService.findFacilityIds(programId, receiver))
        .thenReturn(Collections.singleton(sender));
    when(validSourcesService.findFacilityIds(programId, other))
        .thenReturn(Collections.singleton(sender));

    assertThat(service.getPartners(sender, programId, TransferDirection.SEND),
        contains(alpha, zulu));
  }

  @Test
  public void shouldNotCallReferenceDataWhenThereAreNoCandidates() {
    when(validDestinationsService.findFacilityIds(programId, sender))
        .thenReturn(Collections.singleton(sender));

    assertThat(service.getPartners(sender, programId, TransferDirection.SEND), is(empty()));
    verifyZeroInteractions(facilityReferenceDataService);
    verify(validSourcesService, never()).findFacilityIds(any(), any());
  }
}
