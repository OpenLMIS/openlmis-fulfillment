package org.openlmis.fulfillment.repository;

import org.openlmis.fulfillment.domain.ConfigurationSetting;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ConfigurationSettingRepository extends
    PagingAndSortingRepository<ConfigurationSetting, String> {
}

