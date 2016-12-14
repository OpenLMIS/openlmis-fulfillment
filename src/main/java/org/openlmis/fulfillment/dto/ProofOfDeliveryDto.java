package org.openlmis.fulfillment.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


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
  @Getter
  @Setter
  private LocalDate receivedDate;

  @Override
  public List<ProofOfDeliveryLineItem.Importer> getProofOfDeliveryLineItems() {
    return new ArrayList<>(
        Optional.ofNullable(proofOfDeliveryLineItems).orElse(Collections.emptyList())
    );
  }

  /**
   * Create new list of ProofOfDeliveryDto based on given list of {@link ProofOfDelivery}
   * @param proofOfDeliveries instance of ProofOfDelivery
   * @return new instance ProofOfDeliveryDto.
   */
  public static Collection<ProofOfDeliveryDto> newInstance(
      Iterable<ProofOfDelivery> proofOfDeliveries) {

    Collection<ProofOfDeliveryDto> proofOfDeliveryDtos = new ArrayList<>();
    proofOfDeliveries.forEach(pod -> proofOfDeliveryDtos.add(newInstance(pod)));
    return proofOfDeliveryDtos;
  }

  /**
   * Create new instance of ProofOfDeliveryDto based on given {@link ProofOfDelivery}
   * @param proofOfDelivery instance of ProofOfDelivery
   * @return new instance od ProofOfDeliveryDto.
   */
  public static ProofOfDeliveryDto newInstance(ProofOfDelivery proofOfDelivery) {
    ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
    proofOfDelivery.export(proofOfDeliveryDto);

    proofOfDeliveryDto.setOrder(OrderDto.newInstance(proofOfDelivery.getOrder()));

    if (proofOfDelivery.getProofOfDeliveryLineItems() != null) {
      proofOfDeliveryDto.setProofOfDeliveryLineItems(proofOfDelivery.getProofOfDeliveryLineItems()
          .stream().map(ProofOfDeliveryLineItemDto::newInstance).collect(Collectors.toList()));
    }
    return proofOfDeliveryDto;
  }
}
