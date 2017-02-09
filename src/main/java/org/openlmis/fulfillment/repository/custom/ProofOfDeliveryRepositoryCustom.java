package org.openlmis.fulfillment.repository.custom;

import org.openlmis.fulfillment.domain.ProofOfDelivery;

import java.util.UUID;

public interface ProofOfDeliveryRepositoryCustom {

  ProofOfDelivery findByExternalId(UUID externalId);

}

