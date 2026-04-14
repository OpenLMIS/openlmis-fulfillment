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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Flyway.class)
public class ExtensionFlywayMigrationInitializerTest {

  @Mock
  private DataSource dataSource;

  private ExtensionFlywayMigrationInitializer initializer;

  @Before
  public void setUp() {
    initializer = spy(new ExtensionFlywayMigrationInitializer());
    ReflectionTestUtils.setField(initializer, "dataSource", dataSource);
    ReflectionTestUtils.setField(
        initializer, "schemaName", "fulfillment");
  }

  @Test
  public void shouldSkipWhenNoExtensionMigrationsExist() {
    doReturn(false).when(initializer).extensionMigrationsExist();

    initializer.afterPropertiesSet();

    verifyNoInteractions(dataSource);
  }

  @Test
  public void shouldRunMigrationsWhenExtensionMigrationsExist() {
    doReturn(true).when(initializer).extensionMigrationsExist();

    FluentConfiguration mockConfig =
        mock(FluentConfiguration.class, Answers.RETURNS_SELF);
    Flyway mockFlyway = mock(Flyway.class);

    PowerMockito.mockStatic(Flyway.class);
    PowerMockito.when(Flyway.configure()).thenReturn(mockConfig);
    when(mockConfig.load()).thenReturn(mockFlyway);
    when(mockFlyway.migrate()).thenReturn(1);

    initializer.afterPropertiesSet();

    verify(mockConfig).dataSource(dataSource);
    verify(mockConfig).schemas("fulfillment");
    verify(mockConfig).table(ExtensionFlywayMigrationInitializer.EXTENSION_TABLE);
    verify(mockConfig).locations(ExtensionFlywayMigrationInitializer.EXTENSION_LOCATION);
    verify(mockFlyway).migrate();
  }

}
