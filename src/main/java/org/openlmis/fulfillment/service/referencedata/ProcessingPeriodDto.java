package org.openlmis.fulfillment.service.referencedata;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProcessingPeriodDto {
  private UUID id;
  private ProcessingScheduleDto processingSchedule;
  private String name;
  private String description;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate startDate;
  
  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate endDate;
}
