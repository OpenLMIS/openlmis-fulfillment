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

package org.openlmis.fulfillment.extension;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.Application;
import org.openlmis.fulfillment.domain.Base36EncodedOrderNumberGenerator;
import org.openlmis.fulfillment.extension.point.OrderNumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@SuppressWarnings("PMD.UnusedLocalVariable")
public class ExtensionManagerIntegrationTest {

  private static final String invalidPointId = "InvalidExtensionPoint";
  private static final String invalidExtensionId = "InvalidExtension";
  private static final String extensionId = "Base36EncodedOrderNumberGenerator";

  private HashMap<String, String> extensions;

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Prepare the test environment - add extensions for test purposes.
   */
  @Before
  public void setUp() {
    extensions = new HashMap<>();
    extensions.put(OrderNumberGenerator.POINT_ID, extensionId);
    extensions.put(invalidPointId, invalidExtensionId);
    extensionManager.setExtensions(extensions);
  }

  @Test
  public void testShouldReturnExtensionWithGivenIdAndClass() {
    OrderNumberGenerator orderQuantity = (OrderNumberGenerator) extensionManager
        .getExtension(OrderNumberGenerator.POINT_ID, OrderNumberGenerator.class);
    Assert.assertEquals(orderQuantity.getClass(), Base36EncodedOrderNumberGenerator.class);
  }

  @Test
  public void testShouldReturnExtensionWithGivenClassIfMappingDoesNotExist() {
    OrderNumberGenerator orderQuantity = (OrderNumberGenerator) extensionManager
        .getExtension("test", OrderNumberGenerator.class);
    Assert.assertEquals(orderQuantity.getClass(), Base36EncodedOrderNumberGenerator.class);
  }

  @Test(expected = ExtensionException.class)
  public void testShouldNotReturnExtensionByPointIdWhenInvalidIdAndClass() {
    OrderNumberGenerator orderQuantity = (OrderNumberGenerator) extensionManager
        .getExtension(invalidPointId, getClass());
  }
}
