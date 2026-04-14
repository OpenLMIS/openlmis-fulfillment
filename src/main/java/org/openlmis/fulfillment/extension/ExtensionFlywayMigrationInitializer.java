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

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Runs extension-specific Flyway migrations after the core migrations have completed.
 * Extensions can include SQL migration files in {@code db/extension/} on the classpath.
 * These are tracked in a separate {@code extension_schema_version} table so that
 * extension migrations are independent of the core migration history.
 */
@Component
@DependsOn("flywayInitializer")
public class ExtensionFlywayMigrationInitializer implements InitializingBean {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExtensionFlywayMigrationInitializer.class);

  static final String EXTENSION_LOCATION = "classpath:db/extension";
  static final String EXTENSION_TABLE = "extension_schema_version";
  static final String EXTENSION_DIR = "db/extension";

  @Autowired
  private DataSource dataSource;

  @Value("${spring.flyway.schemas:fulfillment}")
  private String schemaName;

  /**
   * Checks for extension migrations on the classpath and runs them if present.
   * Does nothing when no extension migration directory is found.
   */
  @Override
  public void afterPropertiesSet() {
    if (!extensionMigrationsExist()) {
      LOGGER.debug("No extension migrations found on classpath; skipping.");
      return;
    }

    LOGGER.info("Extension migrations detected; running extension Flyway.");

    Flyway extensionFlyway = Flyway.configure()
        .dataSource(dataSource)
        .schemas(schemaName)
        .table(EXTENSION_TABLE)
        .locations(EXTENSION_LOCATION)
        .sqlMigrationPrefix("")
        .placeholderPrefix("#[")
        .placeholderSuffix("]")
        .ignoreMissingMigrations(true)
        .baselineOnMigrate(true)
        .load();

    int applied = extensionFlyway.migrate();
    LOGGER.info("Extension Flyway applied {} migration(s).", applied);
  }

  /**
   * Checks whether the extension migration directory exists on the classpath.
   */
  boolean extensionMigrationsExist() {
    return new ClassPathResource(EXTENSION_DIR).exists();
  }
}
