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

package org.openlmis.fulfillment.util;

public abstract class ConfigurationSettingKeys {

  public static final String FULFILLMENT_EMAIL_NOREPLY
      = "fulfillment.email.noreply";
  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT
      = "fulfillment.email.order-creation.subject";
  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_BODY
      = "fulfillment.email.order-creation.body";

  private ConfigurationSettingKeys() {
    throw new UnsupportedOperationException();
  }

}
