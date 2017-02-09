package org.openlmis.fulfillment.repository.custom;

import org.openlmis.fulfillment.domain.ProofOfDelivery;

import java.util.List;
import java.util.UUID;

public interface ProofOfDeliveryRepositoryCustom {

  List<ProofOfDelivery> searchByExternalId(UUID externalId);
  
}

