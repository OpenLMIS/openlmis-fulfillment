package org.openlmis.order.repository;

import org.openlmis.order.domain.TemplateParameter;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface TemplateParameterRepository extends
    PagingAndSortingRepository<TemplateParameter, UUID> {
}

