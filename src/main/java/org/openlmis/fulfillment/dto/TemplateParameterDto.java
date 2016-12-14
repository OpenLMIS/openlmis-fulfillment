package org.openlmis.fulfillment.dto;


import org.openlmis.fulfillment.domain.TemplateParameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateParameterDto implements TemplateParameter.Importer,
    TemplateParameter.Exporter {

  private UUID id;
  private String name;
  private String displayName;
  private String defaultValue;
  private String dataType;
  private String selectSql;
  private String description;
}
