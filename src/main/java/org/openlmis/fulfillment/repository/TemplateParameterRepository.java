package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.TemplateParameter;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface TemplateParameterRepository extends
    PagingAndSortingRepository<TemplateParameter, UUID> {
}

