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

package org.openlmis.fulfillment.web.stockmanagement;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;
import org.openlmis.fulfillment.domain.naming.VvmStatus;
import org.openlmis.fulfillment.testutils.ToStringTestUtils;

public class StockEventLineItemDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier.forClass(StockEventLineItemDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    StockEventLineItemDto stockEventDto = new StockEventLineItemDtoDataBuilder().build();
    ToStringTestUtils.verify(StockEventLineItemDto.class, stockEventDto);
  }

  @Test
  public void shouldAddVvmStatusToExtraData() {
    StockEventLineItemDto stockEventDto = new StockEventLineItemDtoDataBuilder().build();
    stockEventDto.setVvmStatus(VvmStatus.STAGE_3);

    assertThat(stockEventDto.getExtraData(), hasEntry("vvmStatus", "STAGE_3"));
  }

  @Test
  public void shouldNotModifyExtraDataIfVvmStatusIsNull() {
    StockEventLineItemDto stockEventDto = new StockEventLineItemDtoDataBuilder().build();
    stockEventDto.setVvmStatus(null);

    assertThat(stockEventDto.getExtraData(), not(hasKey("vvmStatus")));
  }
}
