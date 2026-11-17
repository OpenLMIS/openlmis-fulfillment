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

package org.openlmis.fulfillment.domain;

/**
 * How an order came to exist. Stored on {@link Order} so that consumers can tell transfers apart
 * from requisition-based and requisition-less orders without inspecting other fields.
 */
public enum OrderType {
  /** Created from an approved requisition (has an externalId). */
  REQUISITION,
  /** Created manually through the requisition-less order endpoints. */
  REQUISITION_LESS,
  /** A facility requests stock from a transfer partner. */
  TRANSFER_REQUEST,
  /** A facility pushes stock to a transfer partner without a prior request. */
  TRANSFER_PUSH
}
