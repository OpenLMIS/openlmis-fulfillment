package org.openlmis.fulfillment.repository.custom.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.openlmis.fulfillment.domain.Order.EXTERNAL_ID;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.custom.ProofOfDeliveryRepositoryCustom;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ProofOfDeliveryRepositoryImpl implements ProofOfDeliveryRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Finds proof Of Deliveries related with the given external id. If property is null an empty
   * list will be returned.
   */
  @Override
  public ProofOfDelivery findByExternalId(UUID externalId) {
    checkNotNull(externalId);

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ProofOfDelivery> query = builder.createQuery(ProofOfDelivery.class);
    Root<ProofOfDelivery> pod = query.from(ProofOfDelivery.class);
    Join<ProofOfDelivery, Order> order = pod.join(ProofOfDelivery.ORDER);

    Predicate predicate = builder.conjunction();
    predicate = builder.and(predicate, builder.equal(order.get(EXTERNAL_ID), externalId));

    query.where(predicate);

    try {
      return entityManager.createQuery(query).getSingleResult();
    } catch (NoResultException ignored) {
      return null;
    }
  }

}
