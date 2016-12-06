package org.openlmis.fulfillment;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.openlmis.fulfillment.i18n.ExposedMessageSource;
import org.openlmis.fulfillment.i18n.ExposedMessageSourceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
@SpringBootApplication
@ComponentScan("org.openlmis.fulfillment")
public class Application {

  @Value("${defaultLocale}")
  private Locale defaultLocale;

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
    lr.setDefaultLocale(defaultLocale);
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

}
