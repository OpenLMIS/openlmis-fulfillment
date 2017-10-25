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

import static org.openlmis.fulfillment.domain.Order.CREATED_DATE;
import static org.openlmis.fulfillment.domain.Order.PROCESSING_PERIOD_ID;
import static org.openlmis.fulfillment.domain.Order.PROGRAM_ID;
import static org.openlmis.fulfillment.domain.Order.REQUESTING_FACILITY_ID;
import static org.openlmis.fulfillment.domain.Order.STATUS;
import static org.openlmis.fulfillment.domain.Order.SUPPLYING_FACILITY_ID;
import static org.springframework.util.CollectionUtils.isEmpty;

import org.openlmis.fulfillment.domain.Order;
import org.openlmis.fulfillment.domain.OrderStatus;
import org.openlmis.fulfillment.repository.custom.OrderRepositoryCustom;
import org.openlmis.fulfillment.util.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

  @Autowired
  private DateHelper dateHelper;

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all Orders with matched parameters.
   *
   * @param supplyingFacility  supplyingFacility of searched Orders.
   * @param requestingFacility requestingFacility of searched Orders.
   * @param program            program of searched Orders.
   * @param processingPeriod   UUID of processing period
   * @param statuses           order statuses.
   * @param startDate          start date
   * @param endDate            end date
   * @return List of Orders with matched parameters.
   */
  @Override
  public List<Order> searchOrders(UUID supplyingFacility, UUID requestingFacility,
                                  UUID program, UUID processingPeriod, Set<OrderStatus> statuses,
                                  LocalDate startDate, LocalDate endDate) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Order> query = builder.createQuery(Order.class);
    Root<Order> root = query.from(Order.class);

    Predicate predicate = builder.conjunction();
    predicate = isEqual(SUPPLYING_FACILITY_ID, supplyingFacility, root, predicate, builder);
    predicate = isEqual(REQUESTING_FACILITY_ID, requestingFacility, root, predicate, builder);
    predicate = isEqual(PROGRAM_ID, program, root, predicate, builder);
    predicate = isEqual(PROCESSING_PERIOD_ID, processingPeriod, root, predicate, builder);
    predicate = isOneOf(STATUS, statuses, root, predicate, builder);
    predicate = isBetween(CREATED_DATE, startDate, endDate, root, predicate, builder);

    query.where(predicate);
    query.orderBy(builder.desc(root.get(CREATED_DATE)));

    return entityManager.createQuery(query).getResultList();
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

  private Predicate isBetween(String field, LocalDate start, LocalDate end,
                              Root<Order> root, Predicate predicate, CriteriaBuilder builder) {
    ZonedDateTime from = null;
    ZonedDateTime to = null;

    if (start != null) {
      from = start.atStartOfDay(dateHelper.getZone());
    }

    if (end != null) {
      to = ZonedDateTime.of(end, LocalTime.MAX, dateHelper.getZone());
    }

    if (from != null && to != null) {
      return builder.and(predicate, builder.between(root.get(field), from, to));
    } else if (from != null) {
      return builder.and(predicate, builder.greaterThanOrEqualTo(root.get(field), from));
    } else if (to != null) {
      return builder.and(predicate, builder.lessThanOrEqualTo(root.get(field), to));
    }

    return predicate;
  }

  private Predicate isEqual(String field, Object value, Root<Order> root, Predicate predicate,
                            CriteriaBuilder builder) {
    return value != null
        ? builder.and(predicate, builder.equal(root.get(field), value))
        : predicate;
  }

}
