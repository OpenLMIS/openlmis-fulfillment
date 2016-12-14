package org.openlmis.fulfillment.web;


import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;
import org.openlmis.fulfillment.dto.TemplateDto;
import org.openlmis.fulfillment.dto.TemplateParameterDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemplateDtoBuilder {

  /**
   * Create new list of TemplateDto based on given list of {@link Template}
   * @param templates list of {@link Template}
   * @return new list of TemplateDto.
   */
  public Iterable<TemplateDto> build(Iterable<Template> templates) {

    List<TemplateDto> templateDtos = new ArrayList<>();
    for (Template template: templates) {
      templateDtos.add(build(template));
    }
    return templateDtos;
  }

  /**
   * Create new instance of TemplateDto based on given {@link TemplateDto}
   * @param template instance of Template
   * @return new instance of TemplateDto.
   */
  public TemplateDto build(Template template) {
    if (template == null) {
      return null;
    }
    TemplateDto templateDto = new TemplateDto();
    template.export(templateDto);

    if (template.getTemplateParameters() != null) {
      for (TemplateParameter templateParameter : template.getTemplateParameters()) {
        TemplateParameterDto templateParameterDto = new TemplateParameterDto();
        templateParameter.export(templateParameterDto);
        templateDto.getTemplateParameters().add(templateParameterDto);
      }
    }
    return templateDto;
  }
}
