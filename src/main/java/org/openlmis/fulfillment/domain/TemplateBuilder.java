package org.openlmis.fulfillment.domain;

import java.util.ArrayList;

public class TemplateBuilder {
  /**
   * Create a new instance of Tamplate absed on data from {@link Template.Importer}
   *
   * @param importer instance of {@link Template.Importer}
   * @return new instance od template.
   */
  public static Template newTemplate(Template.Importer importer) {
    Template template = new Template();
    template.setId(importer.getId());
    template.setName(importer.getName());
    template.setData(importer.getData());
    template.setType(importer.getType());
    template.setDescription(importer.getDescription());
    template.setTemplateParameters(new ArrayList<>());

    if (importer.getTemplateParameters() != null) {
      for (TemplateParameter.Importer templateParameter : importer.getTemplateParameters()) {
        TemplateParameter item = TemplateParameter.newTemplateParameter(templateParameter);
        template.getTemplateParameters().add(item);
      }
    }
    return template;
  }
}
