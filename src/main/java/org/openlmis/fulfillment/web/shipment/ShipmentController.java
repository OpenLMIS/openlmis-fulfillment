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

package org.openlmis.fulfillment.web.shipment;

import static org.openlmis.fulfillment.service.ResourceNames.BASE_PATH;
import static org.openlmis.fulfillment.web.shipment.ShipmentController.RESOURCE_PATH;

import org.openlmis.fulfillment.domain.CreationDetails;
import org.openlmis.fulfillment.domain.Shipment;
import org.openlmis.fulfillment.repository.ShipmentRepository;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.util.DateHelper;
import org.openlmis.fulfillment.web.BaseController;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.UUID;

@Controller
@Transactional
@RequestMapping(RESOURCE_PATH)
public class ShipmentController extends BaseController {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(ShipmentController.class);

  static final String RESOURCE_PATH = BASE_PATH + "/shipments";

  @Autowired
  private ShipmentRepository shipmentRepository;

  @Autowired
  private ShipmentDtoBuilder shipmentDtoBuilder;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private DateHelper dateHelper;

  /**
   * Allows creating new shipment. If the id is specified, it will be ignored.
   *
   * @param shipmentDto A shipment item bound to the request body.
   * @return created shipment.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ShipmentDto createShipment(@RequestBody ShipmentDto shipmentDto) {
    XLOGGER.entry(shipmentDto);
    Profiler profiler = new Profiler("CREATE_SHIPMENT");
    profiler.setLogger(XLOGGER);

    profiler.start("CREATE_DOMAIN_INSTANCE");
    setShipDetailsToDto(shipmentDto);
    Shipment shipment = Shipment.newInstance(shipmentDto);

    profiler.start("SAVE_AND_CREATE_DTO");
    shipment = shipmentRepository.save(shipment);
    ShipmentDto dto = shipmentDtoBuilder.build(shipment);

    profiler.stop().log();
    XLOGGER.exit(dto);
    return dto;
  }

  private void setShipDetailsToDto(ShipmentDto shipmentDto) {
    UserDto currentUser = authenticationHelper.getCurrentUser();
    UUID userId = currentUser == null ? shipmentDto.getShippedBy().getId() : currentUser.getId();

    shipmentDto.setShipDetails(
        new CreationDetails(userId, dateHelper.getCurrentDateTimeWithSystemZone()));
  }

  /**
   * Get chosen shipment.
   *
   * @param id UUID of shipment item which we want to get
   * @return shipment.
   */
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ShipmentDto getShipment(@PathVariable UUID id) {
    XLOGGER.entry(id);
    Profiler profiler = new Profiler("GET_SHIPMENT_BY_ID");
    profiler.setLogger(XLOGGER);

    profiler.start("FIND_IN_DB");
    Shipment shipment = shipmentRepository.findOne(id);

    profiler.start("CREATE_DTO");
    ShipmentDto dto = shipmentDtoBuilder.build(shipment);

    profiler.stop().log();
    XLOGGER.exit(dto);
    return dto;
  }

}
