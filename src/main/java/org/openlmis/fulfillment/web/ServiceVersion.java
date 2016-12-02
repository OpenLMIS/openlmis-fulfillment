package org.openlmis.fulfillment.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Properties;

/**
 * Class containing version information.
 */
@Getter
class ServiceVersion {
  private static final String VERSION_FILE = "version.properties";

  private String service = "service";
  private String build = "${build}";
  private String branch = "${branch}";
  private String timeStamp = "${time}";
  private String version = "version";

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVersion.class);

  /**
   * Class constructor used to fill Version with data from version file.
   */
  ServiceVersion() {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(VERSION_FILE)) {
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);
        service = properties.getProperty("Service");
        version = properties.getProperty("Version");
        build = properties.getProperty("Build");
        branch = properties.getProperty("Branch");
        timeStamp = properties.getProperty("Timestamp", Instant.now().toString());
      }
    } catch (IOException exp) {
      LOGGER.error("Error loading version properties file", exp);
    }
  }
}
