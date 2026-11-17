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

import java.util.List;
import java.util.UUID;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;

/**
 * Decides which facilities may transfer stock between each other. Callers depend only on this
 * interface so that the rule can be replaced without changing them.
 */
public interface TransferPartnerService {

  /**
   * Returns the facilities the given facility may transfer stock with for the given program.
   *
   * @param facilityId id of the facility asking for partners
   * @param programId  id of the program
   * @param direction  {@link TransferDirection#SEND} for facilities it may send to,
   *                   {@link TransferDirection#REQUEST} for facilities it may request from
   * @return partner facilities, never null
   */
  List<FacilityDto> getPartners(UUID facilityId, UUID programId, TransferDirection direction);

  /**
   * Checks whether the sending facility may transfer stock to the receiving facility for the
   * given program.
   *
   * @param sendingFacilityId   id of the facility the stock leaves (supplying facility)
   * @param receivingFacilityId id of the facility the stock arrives at
   * @param programId           id of the program
   * @return true if the transfer is allowed
   */
  boolean isValidPair(UUID sendingFacilityId, UUID receivingFacilityId, UUID programId);
}
