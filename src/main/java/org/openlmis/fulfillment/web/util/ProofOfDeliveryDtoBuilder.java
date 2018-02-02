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

package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Shipment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProofOfDeliveryDtoBuilder {

  @Value("${service.url}")
  private String serviceUrl;

  /**
   * Create a new list of {@link ProofOfDeliveryDto} based on data
   * from {@link Shipment}.
   *
   * @param pods collection used to create {@link ProofOfDeliveryDto} list (can be {@code null})
   * @return new list of {@link ProofOfDeliveryDto}. Empty list if passed argument is {@code null}.
   */
  public List<ProofOfDeliveryDto> build(Collection<ProofOfDelivery> pods) {
    if (null == pods) {
      return Collections.emptyList();
    }
    return pods
        .stream()
        .map(this::export)
        .collect(Collectors.toList());
  }

  /**
   * Create a new instance of {@link ProofOfDeliveryDto} based on data
   * from {@link ProofOfDelivery}.
   *
   * @param pod instance used to create {@link ProofOfDeliveryDto} (can be {@code null})
   * @return new instance of {@link ProofOfDeliveryDto}. {@code null}
   *         if passed argument is {@code null}.
   */
  public ProofOfDeliveryDto build(ProofOfDelivery pod) {
    if (null == pod) {
      return null;
    }
    return export(pod);
  }

  private ProofOfDeliveryDto export(ProofOfDelivery pod) {
    ProofOfDeliveryDto dto = new ProofOfDeliveryDto();
    dto.setServiceUrl(serviceUrl);
    pod.export(dto);

    return dto;
  }

}
