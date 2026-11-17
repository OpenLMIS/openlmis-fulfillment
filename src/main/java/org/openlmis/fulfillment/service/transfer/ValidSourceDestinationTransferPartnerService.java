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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.stockmanagement.ValidDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourceDestinationsStockManagementService;
import org.openlmis.fulfillment.service.stockmanagement.ValidSourcesStockManagementService;
import org.springframework.stereotype.Service;

/**
 * Transfer partners based on the stock management valid sources and valid destinations.
 *
 * <p>A transfer from S to R is allowed only when both rows exist for the program: R is a valid
 * destination of S, and S is a valid source of R. Shipment and proof of delivery stock events
 * need both rows, so a pair missing either of them would fail later. The geographic level
 * affinity of each assignment is applied by stock management. Only reference data facilities are
 * partners; organizations are ignored.
 */
@Service
@RequiredArgsConstructor
public class ValidSourceDestinationTransferPartnerService implements TransferPartnerService {

  private final ValidSourcesStockManagementService validSourcesService;

  private final ValidDestinationsStockManagementService validDestinationsService;

  private final FacilityReferenceDataService facilityReferenceDataService;

  @Override
  public List<FacilityDto> getPartners(UUID facilityId, UUID programId,
      TransferDirection direction) {
    boolean sending = TransferDirection.SEND == direction;

    // candidates come from the facility's own side, each one is then checked from its side
    ValidSourceDestinationsStockManagementService own = sending
        ? validDestinationsService
        : validSourcesService;
    ValidSourceDestinationsStockManagementService partnerSide = sending
        ? validSourcesService
        : validDestinationsService;

    Set<UUID> candidateIds = own.findFacilityIds(programId, facilityId)
        .stream()
        .filter(candidateId -> !Objects.equals(candidateId, facilityId))
        .collect(Collectors.toSet());

    if (candidateIds.isEmpty()) {
      return Collections.emptyList();
    }

    // Stock management answers from the facility type and the geographic zone (affinity is
    // matched along the zone's parents), so candidates sharing both get the same list and one
    // call serves the whole group.
    Map<List<UUID>, Set<UUID>> counterpartsByTypeAndZone = new HashMap<>();

    return facilityReferenceDataService.findByIds(candidateIds)
        .stream()
        .filter(candidate -> counterpartsByTypeAndZone
            .computeIfAbsent(typeAndZone(candidate),
                key -> partnerSide.findFacilityIds(programId, candidate.getId()))
            .contains(facilityId))
        .sorted(Comparator.comparing(FacilityDto::getName,
            Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
        .collect(Collectors.toList());
  }

  private static List<UUID> typeAndZone(FacilityDto facility) {
    return Arrays.asList(
        facility.getType() == null ? null : facility.getType().getId(),
        facility.getGeographicZone() == null ? null : facility.getGeographicZone().getId());
  }

  @Override
  public boolean isValidPair(UUID sendingFacilityId, UUID receivingFacilityId, UUID programId) {
    if (sendingFacilityId == null || receivingFacilityId == null || programId == null
        || sendingFacilityId.equals(receivingFacilityId)) {
      return false;
    }

    return validDestinationsService.findFacilityIds(programId, sendingFacilityId)
        .contains(receivingFacilityId)
        && validSourcesService.findFacilityIds(programId, receivingFacilityId)
        .contains(sendingFacilityId);
  }
}
