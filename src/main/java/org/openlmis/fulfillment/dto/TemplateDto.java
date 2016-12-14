package org.openlmis.fulfillment.dto;

import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.domain.TemplateParameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class TemplateDto implements Template.Importer, Template.Exporter {

  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private String type;

  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private byte[] data;

  @Setter
  private List<TemplateParameterDto> templateParameters;

  @Override
  public List<TemplateParameter.Importer> getTemplateParameters() {
    return new ArrayList<>(
        Optional.ofNullable(templateParameters).orElse(Collections.emptyList())
    );
  }
}
