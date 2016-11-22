package org.openlmis.fulfillment.referencedata.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ProcessingPeriodDto {
  private UUID id;
  private ProcessingScheduleDto processingSchedule;
  private String name;
  private String description;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate startDate;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  private LocalDate endDate;
}
