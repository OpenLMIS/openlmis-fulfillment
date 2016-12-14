package org.openlmis.fulfillment.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.domain.convert.LocalDatePersistenceConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Convert;


@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDeliveryDto implements ProofOfDelivery.Exporter, ProofOfDelivery.Importer {
  @Getter
  @Setter
  private UUID id;

  @Getter
  @Setter
  private OrderDto order;

  @Setter
  private List<ProofOfDeliveryLineItemDto> proofOfDeliveryLineItems;

  @Getter
  @Setter
  private Integer totalShippedPacks;

  @Getter
  @Setter
  private Integer totalReceivedPacks;

  @Getter
  @Setter
  private Integer totalReturnedPacks;

  @Getter
  @Setter
  private String deliveredBy;

  @Getter
  @Setter
  private String receivedBy;

  @JsonSerialize(using = LocalDateSerializer.class)
  @JsonDeserialize(using = LocalDateDeserializer.class)
  @Convert(converter = LocalDatePersistenceConverter.class)
  @Getter
  @Setter
  private LocalDate receivedDate;

  @Override
  public List<ProofOfDeliveryLineItem.Importer> getProofOfDeliveryLineItems() {
    return new ArrayList<>(
        Optional.ofNullable(proofOfDeliveryLineItems).orElse(Collections.emptyList())
    );
  }
}
