package org.openlmis.fulfillment.repository;

import org.junit.Before;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public class TemplateParameterRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<TemplateParameter> {

  @Autowired
  private TemplateRepository templateRepository;

  @Autowired
  private TemplateParameterRepository templateParameterRepository;

  private Template template = new Template();

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    template.setName("TemplateRepositoryIntegrationTest");
    templateRepository.save(template);
  }

  @Override
  CrudRepository<TemplateParameter, UUID> getRepository() {
    return templateParameterRepository;
  }

  @Override
  TemplateParameter generateInstance() {
    TemplateParameter templateParameter = new TemplateParameter();
    templateParameter.setTemplate(template);

    return templateParameter;
  }
}
