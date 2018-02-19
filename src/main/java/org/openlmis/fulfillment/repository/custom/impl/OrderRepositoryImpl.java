/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.repository.custom.impl;

import static org.openlmis.fulfillment.domain.Order.ORDER_STATUS;
import static org.openlmis.fulfillment.domain.Order.PROCESSING_PERIOD_ID;
import static org.openlmis.fulfillment.domain.Order.PROGRAM_ID;
import static org.openlmis.fulfillment.domain.Order.REQUESTING_FACILITY_ID;
import static org.openlmis.fulfillment.domain.Order.SUPPLYING_FACILITY_ID;
import static org.springframework.util.CollectionUtils.isEmpty;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.custom.OrderRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all Orders with matched parameters.
   *
   * @param supplyingFacilities set of supplyingFacility of searched Orders.
   * @param requestingFacility  requestingFacility of searched Orders.
   * @param program             program of searched Orders.
   * @param processingPeriod    UUID of processing period
   * @param statuses            order statuses.
   * @param pageable            page parameters
   * @return List of Orders with matched parameters.
   */
  @Override
  public Page<Order> searchOrders(Set<UUID> supplyingFacilities, UUID requestingFacility,
                                  UUID program, UUID processingPeriod, Set<OrderStatus> statuses,
                                  Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Order> query = builder.createQuery(Order.class);
    query = prepareQuery(query, supplyingFacilities, requestingFacility,
        program, processingPeriod, statuses, pageable, false);
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    countQuery = prepareQuery(countQuery, supplyingFacilities, requestingFacility,
        program, processingPeriod, statuses, pageable, true);

    Long count = entityManager.createQuery(countQuery).getSingleResult();
    List<Order> result = entityManager.createQuery(query)
        .setMaxResults(pageable.getPageSize())
        .setFirstResult(pageable.getPageSize() * pageable.getPageNumber())
        .getResultList();

    return new PageImpl<>(result, pageable, count);
  }

  /**
   * Retrieves the distinct UUIDs of the available requesting facilities.
   */
  @Override
  public List<UUID> getRequestingFacilities(UUID supplyingFacility) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<UUID> query = builder.createQuery(UUID.class);
    Root<Order> root = query.from(Order.class);

    if (null != supplyingFacility) {
      query.where(builder.equal(root.get(SUPPLYING_FACILITY_ID), supplyingFacility));
    }

    query.select(root.get(REQUESTING_FACILITY_ID)).distinct(true);

    return entityManager.createQuery(query).getResultList();
  }

  private <T> CriteriaQuery<T> prepareQuery(CriteriaQuery<T> query, Set<UUID> supplyingFacilities,
                                            UUID requestingFacility, UUID program,
                                            UUID processingPeriod, Set<OrderStatus> statuses,
                                            Pageable pageable, boolean count) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    Root<Order> root = query.from(Order.class);

    if (count) {
      CriteriaQuery<Long> countQuery = (CriteriaQuery<Long>) query;
      query = (CriteriaQuery<T>) countQuery.select(builder.count(root));
    }

    Predicate predicate = builder.conjunction();
    predicate = isOneOf(SUPPLYING_FACILITY_ID, supplyingFacilities, root, predicate, builder);
    predicate = isEqual(REQUESTING_FACILITY_ID, requestingFacility, root, predicate, builder);
    predicate = isEqual(PROGRAM_ID, program, root, predicate, builder);
    predicate = isEqual(PROCESSING_PERIOD_ID, processingPeriod, root, predicate, builder);
    predicate = isOneOf(ORDER_STATUS, statuses, root, predicate, builder);

    query.where(predicate);

    if (!count && pageable != null && pageable.getSort() != null) {
      query = addSortProperties(query, root, pageable);
    }

    return query;
  }

  private Predicate isOneOf(String field, Collection collection, Root<Order> root,
                            Predicate predicate, CriteriaBuilder builder) {
    if (!isEmpty(collection)) {
      Predicate collectionPredicate = builder.disjunction();

      for (Object elem : collection) {
        collectionPredicate = builder.or(collectionPredicate, builder.equal(root.get(field), elem));
      }

      return builder.and(predicate, collectionPredicate);
    }

    return predicate;
  }

  private Predicate isEqual(String field, Object value, Root<Order> root, Predicate predicate,
                            CriteriaBuilder builder) {
    return value != null
        ? builder.and(predicate, builder.equal(root.get(field), value))
        : predicate;
  }

  private <T> CriteriaQuery<T> addSortProperties(CriteriaQuery<T> query,
                                                 Root root, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    List<javax.persistence.criteria.Order> orders = new ArrayList<>();
    Iterator<Sort.Order> iterator = pageable.getSort().iterator();
    Sort.Order order;

    while (iterator.hasNext()) {
      order = iterator.next();
      String property = order.getProperty();

      Path path = root.get(property);
      if (order.isAscending()) {
        orders.add(builder.asc(path));
      } else {
        orders.add(builder.desc(path));
      }
    }
    return query.orderBy(orders);
  }
}
