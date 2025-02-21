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

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.repository.custom.ProofOfDeliveryRepositoryCustom;
import org.openlmis.fulfillment.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class ProofOfDeliveryRepositoryImpl implements ProofOfDeliveryRepositoryCustom {

  private static final String POD_SELECT = "SELECT DISTINCT p"
      + " FROM ProofOfDelivery AS p"
      + " INNER JOIN FETCH p.shipment AS s"
      + " INNER JOIN FETCH s.order AS o"
      + " LEFT JOIN FETCH p.lineItems";

  private static final String POD_COUNT = "SELECT DISTINCT COUNT(*)"
      + " FROM ProofOfDelivery AS p"
      + " INNER JOIN p.shipment AS s"
      + " INNER JOIN s.order AS o";

  private static final String WHERE = "WHERE";
  private static final String AND = " AND ";
  private static final String OR = " OR ";
  private static final String ASC = "ASC";
  private static final String DESC = "DESC";
  private static final String ORDER_BY = "ORDER BY";
  private static final String WITH_SHIPMENT_ID = "s.id = :shipmentId";
  private static final String WITH_ORDER_ID = "o.id = :orderId";
  private static final String WITH_RECEIVING_FACILITIES =
      "o.receivingFacilityId IN (:receivingFacilityIds)";
  private static final String WITH_SUPPLYING_FACILITIES =
      "o.supplyingFacilityId IN (:supplyingFacilityIds)";
  private static final String WITH_PROGRAM_IDS =
      "o.programId IN (:programIds)";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method retrieves all Proofs of Delivery (PODs) matching the provided parameters.
   * The receiving and supplying facility IDs are used in `OR` clauses to match the associated
   * facilities.
   *
   * @param shipmentId           UUID of the associated shipment.
   * @param orderId              UUID of the associated order.
   * @param receivingFacilityIds List of UUIDs of receiving facilities in the associated order.
   * @param supplyingFacilityIds List of UUIDs of supplying facilities in the associated order.
   * @param programIds           List of UUIDs of programs associated with the order.
   * @param pageable             Pagination parameters to limit the result set.
   * @return A list of Proofs of Delivery (PODs) matching the provided parameters.
   */
  public Page<ProofOfDelivery> search(
      UUID shipmentId,
      UUID orderId,
      Set<UUID> receivingFacilityIds,
      Set<UUID> supplyingFacilityIds,
      Set<UUID> programIds,
      Pageable pageable
  ) {
    TypedQuery<Long> countQuery = prepareQuery(
        POD_COUNT, shipmentId, orderId, receivingFacilityIds,
        supplyingFacilityIds, programIds, pageable, Long.class
    );
    Long count = countQuery.getSingleResult();

    if (count > 0) {
      TypedQuery<ProofOfDelivery> searchQuery = prepareQuery(
          POD_SELECT, shipmentId, orderId, receivingFacilityIds,
          supplyingFacilityIds, programIds, pageable, ProofOfDelivery.class
      );

      List<ProofOfDelivery> pods = searchQuery
          .setMaxResults(pageable.getPageSize())
          .setFirstResult(Math.toIntExact(pageable.getOffset()))
          .getResultList();

      return Pagination.getPage(pods, pageable, count);
    }

    return Pagination.getPage(emptyList(), pageable, count);
  }

  private <T> TypedQuery<T> prepareQuery(
      String select,
      UUID shipmentId,
      UUID orderId,
      Set<UUID> receivingFacilityIds,
      Set<UUID> supplyingFacilityIds,
      Set<UUID> programIds,
      Pageable pageable,
      Class<T> resultClass
  ) {
    Map<String, Object> params = new HashMap<>();
    List<String> whereClauses = buildWhereClauses(
        shipmentId, orderId, receivingFacilityIds,
        supplyingFacilityIds, programIds, params
    );

    String query = buildQuery(select, whereClauses, pageable);

    TypedQuery<T> typedQuery = entityManager.createQuery(query, resultClass);
    params.forEach(typedQuery::setParameter);

    return typedQuery;
  }

  private List<String> buildWhereClauses(
      UUID shipmentId,
      UUID orderId,
      Set<UUID> receivingFacilityIds,
      Set<UUID> supplyingFacilityIds,
      Set<UUID> programIds,
      Map<String, Object> params
  ) {
    List<String> whereClauses = new ArrayList<>();

    addCondition(whereClauses, params, shipmentId, "shipmentId", WITH_SHIPMENT_ID);
    addCondition(whereClauses, params, orderId, "orderId", WITH_ORDER_ID);

    List<String> orConditions = new ArrayList<>();
    addCondition(orConditions, params, receivingFacilityIds,
        "receivingFacilityIds", WITH_RECEIVING_FACILITIES);
    addCondition(orConditions, params, supplyingFacilityIds,
        "supplyingFacilityIds", WITH_SUPPLYING_FACILITIES);

    if (!orConditions.isEmpty()) {
      whereClauses.add("(" + String.join(OR, orConditions) + ")");
    }

    addCondition(whereClauses, params, programIds, "programIds", WITH_PROGRAM_IDS);
    return whereClauses;
  }

  private void addCondition(
      List<String> whereClauses,
      Map<String, Object> params,
      Object paramValue,
      String paramName,
      String condition
  ) {
    if (isParamValuePresent(paramValue)) {
      whereClauses.add(condition);
      params.put(paramName, paramValue);
    }
  }

  private boolean isParamValuePresent(Object paramValue) {
    return paramValue != null
        && !(paramValue instanceof Collection
        && ((Collection<?>) paramValue).isEmpty());
  }

  private String buildQuery(
      String select,
      List<String> whereClauses,
      Pageable pageable
  ) {
    List<String> sql = new ArrayList<>();
    sql.add(select);

    if (!whereClauses.isEmpty()) {
      sql.add(WHERE);
      sql.add(String.join(AND, whereClauses));
    }

    if (select.equals(POD_SELECT) && pageable.getSort().isSorted()) {
      sql.add(ORDER_BY);
      sql.add(getOrderPredicate(pageable));
    }

    return String.join(" ", sql);
  }

  private String getOrderPredicate(Pageable pageable) {
    List<String> orderPredicates = new ArrayList<>();

    for (Sort.Order order : pageable.getSort()) {
      String direction = order.getDirection().isAscending() ? ASC : DESC;
      orderPredicates.add("p." + order.getProperty() + " " + direction);
    }

    if (orderPredicates.isEmpty()) {
      return "";
    }

    return String.join(",", orderPredicates);
  }
}
