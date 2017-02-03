package org.openlmis.fulfillment.web.util;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.service.ExporterBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProofOfDeliveryDto implements ProofOfDelivery.Exporter, ProofOfDelivery.Importer {
  private UUID id;
  private OrderDto order;
  private List<ProofOfDeliveryLineItemDto> proofOfDeliveryLineItems;
  private String deliveredBy;
  private String receivedBy;
  private ZonedDateTime receivedDate;

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
      Iterable<ProofOfDelivery> proofOfDeliveries, ExporterBuilder exporter) {

    Collection<ProofOfDeliveryDto> proofOfDeliveryDtos = new ArrayList<>();
    proofOfDeliveries.forEach(pod -> proofOfDeliveryDtos.add(newInstance(pod, exporter)));
    return proofOfDeliveryDtos;
  }

  /**
   * Create new instance of ProofOfDeliveryDto based on given {@link ProofOfDelivery}
   * @param proofOfDelivery instance of ProofOfDelivery
   * @return new instance od ProofOfDeliveryDto.
   */
  public static ProofOfDeliveryDto newInstance(ProofOfDelivery proofOfDelivery,
                                               ExporterBuilder exporter) {
    ProofOfDeliveryDto proofOfDeliveryDto = new ProofOfDeliveryDto();
    proofOfDelivery.export(proofOfDeliveryDto);

    proofOfDeliveryDto.setOrder(OrderDto.newInstance(proofOfDelivery.getOrder(), exporter));

    if (proofOfDelivery.getProofOfDeliveryLineItems() != null) {
      proofOfDeliveryDto.setProofOfDeliveryLineItems(proofOfDelivery.getProofOfDeliveryLineItems()
          .stream().map(item -> ProofOfDeliveryLineItemDto.newInstance(item, exporter))
          .collect(Collectors.toList()));
    }
    return proofOfDeliveryDto;
  }
}
