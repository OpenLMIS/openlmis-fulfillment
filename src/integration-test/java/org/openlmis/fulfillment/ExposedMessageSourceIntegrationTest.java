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

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.fulfillment.i18n.ExposedMessageSource;
import org.openlmis.fulfillment.i18n.MessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ExposedMessageSourceIntegrationTest {

  @Autowired
  @Qualifier("messageSource")
  private ExposedMessageSource exposedMessageSource;

  private Set<String> propertyEntries;
  private Set<String> constants;

  @Before
  public void setUp() throws IllegalAccessException {
    propertyEntries = getPropertyEntries();
    constants = getConstants(MessageKeys.class);
  }

  @Test
  public void shouldContainAllConstants() {
    for (String key : constants) {
      assertThat("Missing entry in messages_XX.properties", propertyEntries, hasItems(key));
    }
  }

  @Test
  public void shouldContainAllKeys() {
    for (String key : propertyEntries) {
      assertThat("Missing constant value", constants, hasItem(key));
    }

    List<String> left = constants
        .stream()
        .filter(key -> !propertyEntries.contains(key))
        .collect(Collectors.toList());

    assertThat(
        "There are entries in messages_XX.properties without constant values: " + left,
        left, hasSize(0)
    );
  }

  private Set<String> getPropertyEntries() {
    return exposedMessageSource
        .getAllMessages(Locale.ENGLISH)
        .keySet()
        .stream()
        .filter(key -> !startsWith(key, "fulfillment.print.proofOfDelivery"))
        .filter(key -> !startsWith(key, "fulfillment.header"))
        .collect(Collectors.toSet());
  }

  private Set<String> getConstants(Class clazz) throws IllegalAccessException {
    Set<String> set = new HashSet<>();

    for (Field field : clazz.getDeclaredFields()) {
      int modifiers = field.getModifiers();

      if (isPublic(modifiers) && isStatic(modifiers) && isFinal(modifiers)) {
        set.add(String.valueOf(field.get(null)));
      }
    }

    return set;
  }

}
