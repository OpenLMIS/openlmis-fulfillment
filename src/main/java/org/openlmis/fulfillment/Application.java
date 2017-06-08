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

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.flywaydb.core.Flyway;
import org.openlmis.fulfillment.domain.BaseEntity;
import org.openlmis.fulfillment.i18n.ExposedMessageSource;
import org.openlmis.fulfillment.i18n.ExposedMessageSourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

import static org.apache.commons.lang3.LocaleUtils.toLocale;

@SpringBootApplication(scanBasePackages = "org.openlmis.fulfillment")
@EntityScan(basePackageClasses = BaseEntity.class)
public class Application {

  private Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /**
   * Creates new LocaleResolver.
   *
   * @return Created LocalResolver.
   */
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver lr = new CookieLocaleResolver();
    lr.setCookieName("lang");

    Locale systemLocale;
    try {
      systemLocale = toLocale(System.getenv("LOCALE"));
    } catch (IllegalArgumentException ex) {
      systemLocale = Locale.ENGLISH;
    }
    lr.setDefaultLocale(systemLocale);

    return lr;
  }

  /**
   * Creates new MessageSource.
   *
   * @return Created MessageSource.
   */
  @Bean
  public ExposedMessageSource messageSource() {
    ExposedMessageSourceImpl messageSource = new ExposedMessageSourceImpl();
    messageSource.setBasename("classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setUseCodeAsDefaultMessage(true);

    return messageSource;
  }

  /**
   * Creates new LocalValidatorFactoryBean.
   *
   * @return Created LocalValidatorFactoryBean.
   */
  @Bean
  public LocalValidatorFactoryBean validator() {
    return new LocalValidatorFactoryBean();
  }

  /**
   * Creates new camelContext.
   *
   * @return Created camelContext.
   */
  @Bean
  public CamelContext camelContext() {
    return new DefaultCamelContext();
  }

  /**
   * Creates new camelTemplate.
   *
   * @return Created camelTemplate.
   */
  @Bean
  public ProducerTemplate camelTemplate() {
    return camelContext().createProducerTemplate();
  }

  /**
   * Configures the Flyway migration strategy to clean the DB before migration first.  This is used
   * as the default unless the Spring Profile "production" is active.
   *
   * @return the clean-migrate strategy
   */
  @Bean
  @Profile("!production")
  public FlywayMigrationStrategy cleanMigrationStrategy() {
    FlywayMigrationStrategy strategy = new FlywayMigrationStrategy() {
      @Override
      public void migrate(Flyway flyway) {
        logger.info("Using clean-migrate flyway strategy -- production profile not active");
        flyway.clean();
        flyway.migrate();
      }
    };

    return strategy;
  }

}
