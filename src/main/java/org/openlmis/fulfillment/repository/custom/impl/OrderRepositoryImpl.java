package org.openlmis.fulfillment.repository.custom.impl;

import static org.openlmis.fulfillment.domain.Order.PROGRAM_ID;
import static org.openlmis.fulfillment.domain.Order.REQUESTING_FACILITY_ID;
import static org.openlmis.fulfillment.domain.Order.STATUS;
import static org.openlmis.fulfillment.domain.Order.SUPPLYING_FACILITY_ID;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.custom.OrderRepositoryCustom;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all Orders with matched parameters.
   *
   * @param supplyingFacility  supplyingFacility of searched Orders.
   * @param requestingFacility requestingFacility of searched Orders.
   * @param program            program of searched Orders.
   * @param status             order status. One of {@link OrderStatus}.
   * @return List of Orders with matched parameters.
   */
  @Override
  public List<Order> searchOrders(UUID supplyingFacility, UUID requestingFacility,
                                  UUID program, OrderStatus status) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Order> query = builder.createQuery(Order.class);
    Root<Order> root = query.from(Order.class);
    Predicate predicate = builder.conjunction();

    if (supplyingFacility != null) {
      predicate = builder.and(
          predicate, builder.equal(root.get(SUPPLYING_FACILITY_ID), supplyingFacility)
      );
    }

    if (requestingFacility != null) {
      predicate = builder.and(
          predicate, builder.equal(root.get(REQUESTING_FACILITY_ID), requestingFacility)
      );
    }

    if (program != null) {
      predicate = builder.and(predicate, builder.equal(root.get(PROGRAM_ID), program));
    }

    if (status != null) {
      predicate = builder.and(predicate, builder.equal(root.get(STATUS), status));
    }

    query.where(predicate);

    return entityManager.createQuery(query).getResultList();
  }

}
