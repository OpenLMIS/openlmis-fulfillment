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

package org.openlmis.fulfillment;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * PageImplRepresentation offers a convenient substitute for PageImpl.
 * Because the former lacks a default constructor, it is inconvenient to
 * deserialize. PageImplRepresentation may be used in its stead.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class PageImplRepresentation<T> extends PageImpl<T> {
  private static final long serialVersionUID = 1L;

  private boolean last;
  private boolean first;

  private int totalPages;
  private long totalElements;
  private int size;
  private int number;
  private int numberOfElements;

  private Sort sort;

  private List<T> content;

  public PageImplRepresentation() {
    super(new ArrayList<>());
  }



}
