package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;

public class TemplateRepositoryIntegrationTest extends
    BaseCrudRepositoryIntegrationTest<Template> {

  @Autowired
  private TemplateRepository templateRepository;

  protected TemplateRepository getRepository() {
    return this.templateRepository;
  }

  @Override
  protected Template generateInstance() {
    Template template = new Template();
    template.setName("TemplateRepositoryIntegrationTest");
    return template;
  }
}
