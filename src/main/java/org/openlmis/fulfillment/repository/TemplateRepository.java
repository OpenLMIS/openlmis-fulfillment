package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.Template;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TemplateRepository extends PagingAndSortingRepository<Template, UUID> {

  Template findByName(@Param("name") String name);

}
