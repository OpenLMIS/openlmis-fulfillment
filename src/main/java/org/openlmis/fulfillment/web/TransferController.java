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

package org.openlmis.fulfillment.web;

import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_INVALID_PAIR;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_NOTHING_TO_SEND;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_NOT_TRANSFER_REQUEST;
import static org.openlmis.fulfillment.i18n.MessageKeys.TRANSFER_REQUIRED_FIELD;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderLineItem;
import org.openlmis.fulfillment.repository.OrderRepository;
import org.openlmis.fulfillment.service.OrderService;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.service.referencedata.FacilityDto;
import org.openlmis.fulfillment.service.referencedata.FacilityReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.ProgramDto;
import org.openlmis.fulfillment.service.referencedata.ProgramReferenceDataService;
import org.openlmis.fulfillment.service.referencedata.UserDto;
import org.openlmis.fulfillment.service.transfer.TransferDirection;
import org.openlmis.fulfillment.service.transfer.TransferPartnerService;
import org.openlmis.fulfillment.util.AuthenticationHelper;
import org.openlmis.fulfillment.web.util.BaseDto;
import org.openlmis.fulfillment.web.util.OrderDto;
import org.openlmis.fulfillment.web.util.OrderDtoBuilder;
import org.openlmis.fulfillment.web.validator.OrderValidator;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Facility-to-facility stock transfers. A transfer request is an order the requesting facility
 * places with a transfer partner; it follows the requisition-less order flow (create, edit, send)
 * but the facility pair is checked against {@link TransferPartnerService} on every step.
 */
@Controller
@Transactional
@RequiredArgsConstructor
public class TransferController extends BaseController {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TransferController.class);

  private final TransferPartnerService transferPartnerService;

  private final OrderService orderService;

  private final OrderRepository orderRepository;

  private final PermissionService permissionService;

  private final AuthenticationHelper authenticationHelper;

  private final OrderDtoBuilder orderDtoBuilder;

  private final OrderValidator orderValidator;

  private final FacilityReferenceDataService facilityReferenceDataService;

  private final ProgramReferenceDataService programReferenceDataService;

  /**
   * Returns the facilities the given facility may transfer stock with.
   *
   * @param facilityId id of the facility asking for partners
   * @param programId  id of the program
   * @param direction  SEND for facilities it may send to, REQUEST for facilities it may request
   *                   from
   * @return partner facilities
   */
  @GetMapping("/transfers/partners")
  @ResponseBody
  public List<FacilityDto> getPartners(@RequestParam("facilityId") UUID facilityId,
      @RequestParam("programId") UUID programId,
      @RequestParam("direction") TransferDirection direction) {
    permissionService.canViewTransferPartners(facilityId, direction);
    return transferPartnerService.getPartners(facilityId, programId, direction);
  }

  /**
   * Creates a transfer request. The facility and the receiving facility are always the requesting
   * facility, and the creator is always the current user; values sent for them are ignored.
   * If the id is specified, it will be ignored.
   *
   * @param orderDto transfer request bound to the request body
   * @return created transfer request
   */
  @PostMapping("/transfers/requests")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public OrderDto createTransferRequest(@RequestBody OrderDto orderDto) {
    UUID requestingFacilityId = requireId(orderDto.getRequestingFacility(),
        "requestingFacility");
    UUID supplyingFacilityId = requireId(orderDto.getSupplyingFacility(), "supplyingFacility");
    UUID programId = requireId(orderDto.getProgram(), "program");

    permissionService.canCreateTransferRequest(requestingFacilityId);
    validatePair(supplyingFacilityId, requestingFacilityId, programId);

    // a transfer request is always for, and received by, the requesting facility
    orderDto.setFacility(orderDto.getRequestingFacility());
    orderDto.setReceivingFacility(orderDto.getRequestingFacility());
    orderDto.setId(null);
    orderDto.setExternalId(null);
    orderDto.setQuotedCost(BigDecimal.ZERO);
    orderDto.setEmergency(Boolean.FALSE);

    UUID userId = getCurrentUserId(orderDto);
    // the creator is always the caller, so the client does not have to (and cannot) set it
    orderDto.setCreatedBy(new UserDto(userId));

    Order order = orderService.createTransferRequest(orderDto, userId);

    return orderDtoBuilder.build(order);
  }

  /**
   * Updates line items of a transfer request in CREATING status.
   *
   * @param orderId  id of the transfer request
   * @param orderDto transfer request bound to the request body
   * @return updated transfer request
   */
  @PutMapping("/transfers/requests/{id}")
  @ResponseBody
  public OrderDto updateTransferRequest(@PathVariable("id") UUID orderId,
      @RequestBody OrderDto orderDto, BindingResult bindingResult) {
    checkCanEdit(orderId, orderDto, bindingResult);

    Order order = orderService.updateOrder(orderId, orderDto, getCurrentUserId(orderDto));

    return orderDtoBuilder.build(order);
  }

  /**
   * Saves and sends a transfer request to the supplying facility: ordered quantities are
   * converted to packs and the order becomes ORDERED. At least one line item must have a
   * quantity greater than zero.
   *
   * @param orderId  id of the transfer request
   * @param orderDto transfer request bound to the request body
   */
  @PutMapping("/transfers/requests/{id}/send")
  @ResponseBody
  public void sendTransferRequest(@PathVariable("id") UUID orderId,
      @RequestBody OrderDto orderDto, BindingResult bindingResult) {
    checkCanEdit(orderId, orderDto, bindingResult);

    boolean hasQuantityToSend = orderDto.getOrderLineItems()
        .stream()
        .map(OrderLineItem.Importer::getOrderedQuantity)
        .anyMatch(quantity -> quantity != null && quantity > 0);
    if (!hasQuantityToSend) {
      throw new ValidationException(TRANSFER_NOTHING_TO_SEND, orderId.toString());
    }

    Order order = orderService.updateOrder(orderId, orderDto, getCurrentUserId(orderDto));
    orderService.convertOrderedQuantitiesToPacks(order);
    order.prepareToLocalFulfill();

    orderRepository.save(order);
    XLOGGER.debug("Sent transfer request with id: {}", orderId);
  }

  private void checkCanEdit(UUID orderId, OrderDto orderDto, BindingResult bindingResult) {
    Order existing = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    if (!existing.isTransferRequest()) {
      throw new ValidationException(TRANSFER_NOT_TRANSFER_REQUEST, orderId.toString());
    }

    permissionService.canCreateTransferRequest(existing.getRequestingFacilityId());
    validatePair(existing.getSupplyingFacilityId(), existing.getRequestingFacilityId(),
        existing.getProgramId());

    orderValidator.validate(orderDto, bindingResult);
    throwValidationExceptionIfHasError(bindingResult);
  }

  private void validatePair(UUID supplyingFacilityId, UUID requestingFacilityId,
      UUID programId) {
    if (!transferPartnerService.isValidPair(supplyingFacilityId, requestingFacilityId,
        programId)) {
      throw new ValidationException(TRANSFER_INVALID_PAIR,
          describe(supplyingFacilityId, facilityReferenceDataService::findOne,
              FacilityDto::getName),
          describe(requestingFacilityId, facilityReferenceDataService::findOne,
              FacilityDto::getName),
          describe(programId, programReferenceDataService::findOne, ProgramDto::getName));
    }
  }

  // used only to build the error message, so a failing lookup falls back to the id
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private <T> String describe(UUID id, Function<UUID, T> finder, Function<T, String> name) {
    try {
      T resource = finder.apply(id);
      if (resource != null && name.apply(resource) != null) {
        return name.apply(resource);
      }
    } catch (RuntimeException ex) {
      XLOGGER.warn("Unable to fetch resource {} for the transfer error message", id, ex);
    }
    return String.valueOf(id);
  }

  private UUID requireId(BaseDto reference, String field) {
    if (reference == null || reference.getId() == null) {
      throw new ValidationException(TRANSFER_REQUIRED_FIELD, field);
    }
    return reference.getId();
  }

  private UUID getCurrentUserId(OrderDto orderDto) {
    UserDto currentUser = authenticationHelper.getCurrentUser();
    return currentUser == null ? orderDto.getLastUpdater().getId() : currentUser.getId();
  }
}
