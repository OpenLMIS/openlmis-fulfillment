package org.openlmis.fulfillment.repository.custom.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.openlmis.fulfillment.domain.FacilityFtpSetting;
import org.openlmis.fulfillment.repository.custom.FacilityFtpSettingRepositoryCustom;

public class FacilityFtpSettingRepositoryImpl implements FacilityFtpSettingRepositoryCustom {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<FacilityFtpSetting> searchFacilityFtpSettings(UUID facility) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<FacilityFtpSetting> query = builder.createQuery(FacilityFtpSetting.class);
    Root<FacilityFtpSetting> root = query.from(FacilityFtpSetting.class);
    Predicate predicate = builder.conjunction();

    if (facility != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("facilityId"), facility));
    }

    query.where(predicate);
    return  entityManager.createQuery(query).getResultList();
  }
}
