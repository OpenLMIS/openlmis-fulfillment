package org.openlmis.order.service;

import org.openlmis.order.domain.OrderFileTemplate;
import org.openlmis.order.repository.OrderFileTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderFileTemplateService {

  @Autowired
  private OrderFileTemplateRepository orderFileTemplateRepository;

  /**
   * Get orderFileTemplate.
   *
   * @return OrderFileTemplate.
   */
  public OrderFileTemplate getOrderFileTemplate() {
    Iterable<OrderFileTemplate> orderFileTemplates = orderFileTemplateRepository.findAll();

    if (orderFileTemplates != null && orderFileTemplates.iterator().hasNext()) {
      return orderFileTemplates.iterator().next();
    }

    return null;
  }
}
