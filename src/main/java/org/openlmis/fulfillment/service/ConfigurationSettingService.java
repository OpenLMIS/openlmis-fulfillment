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

package org.openlmis.fulfillment.service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class ConfigurationSettingService {

  static final String POD_REASON = "reasons.pod";
  static final String SHIPMENT_REASON = "reasons.shipment";
  private static final String FTP_TRANSFER = "ftp.transfer.on.requisition.to.order";
  private static final String SEND_EMAIL = "send.email.on.requisition.to.order";

  @Autowired
  private Environment env;

  /**
   * Returns reason id which should be used in proof of delivery - stock replenishment
   * in receiving facility, by default Transfer in - e3fc3cf3-da18-44b0-a220-77c985202e06.
   */
  public UUID getReasonIdForProofOfDelivery() {
    return UUID.fromString(Objects.requireNonNull(env.getProperty(POD_REASON)));
  }

  /**
   * Returns reason id which should be used in shipment - stock consumption
   * in supplying facility, if not present, returns null.
   */
  public UUID getReasonIdForShipment() {
    return Optional.ofNullable(env.getProperty(SHIPMENT_REASON))
        .filter(id -> !id.isEmpty())
        .map(UUID::fromString)
        .orElse(null);
  }

  public String getAllowFtpTransferOnRequisitionToOrder() {
    return env.getProperty(FTP_TRANSFER);
  }

  public String getAllowSendingEmailOnRequisitionToOrder() {
    return env.getProperty(SEND_EMAIL);
  }

}
