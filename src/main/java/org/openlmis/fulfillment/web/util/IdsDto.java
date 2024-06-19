package org.openlmis.fulfillment.web.util;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdsDto {
  private List<UUID> ids;
}
