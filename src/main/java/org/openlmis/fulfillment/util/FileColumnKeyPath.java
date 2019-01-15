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

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;

public enum FileColumnKeyPath {
  ORDERABLE_ID("orderableId"),
  PRODUCT_CODE("productCode"),

  ORDER_ID("orderId"),
  ORDER_CODE("orderCode"),

  QUANTITY_SHIPPED("quantityShipped");

  private String columnPath;

  public static final ImmutableList<FileColumnKeyPath> ORDERABLE_COLUMN_PATHS = new ImmutableList
      .Builder<FileColumnKeyPath>()
      .addAll(asList(ORDERABLE_ID, PRODUCT_CODE))
      .build();

  public static final ImmutableList<FileColumnKeyPath> ORDER_COLUMN_PATHS = new ImmutableList
      .Builder<FileColumnKeyPath>()
      .addAll(asList(ORDER_ID, ORDER_CODE))
      .build();

  public static final ImmutableList<FileColumnKeyPath> QUANTITY_SHIPPED_PATHS = new ImmutableList
      .Builder<FileColumnKeyPath>()
      .addAll(asList(QUANTITY_SHIPPED))
      .build();

  public static final ImmutableList<FileColumnKeyPath> ALL_REQUIRED_COLUMN_PATHS = new ImmutableList
      .Builder<FileColumnKeyPath>()
      .addAll(asList(ORDERABLE_ID, PRODUCT_CODE, ORDER_ID, ORDER_CODE, QUANTITY_SHIPPED))
      .build();

  FileColumnKeyPath(String columnPath) {
    this.columnPath = columnPath;
  }

  @Override
  public String toString() {
    return this.columnPath;
  }

  /**
   * Returns key path enum for entity, path combination.
   *
   * @param path path on the entity.
   */
  public static FileColumnKeyPath fromString(String path) {
    return Arrays.stream(FileColumnKeyPath.values())
        .filter(k -> k.columnPath.equals(path))
        .findFirst()
        .orElse(null);
  }
}