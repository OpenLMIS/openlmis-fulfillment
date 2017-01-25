package org.openlmis.fulfillment.web.util;


import org.openlmis.fulfillment.domain.ExternalStatus;
import org.openlmis.fulfillment.domain.StatusMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusMessageDto implements StatusMessage.Exporter, StatusMessage.Importer {
  private UUID id;
  private UUID authorId;
  private ExternalStatus status;
  private String body;

  /**
   * Create new instance of StatusMessageDto based on given {@link StatusMessage}
   * @param statusMessage instance of StatusMessage
   * @return new instance of StatusMessageDto.
   */
  public static StatusMessageDto newInstance(StatusMessage statusMessage) {
    StatusMessageDto statusMessageDto = new StatusMessageDto();
    statusMessage.export(statusMessageDto);
    return statusMessageDto;
  }
}