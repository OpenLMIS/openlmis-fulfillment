package org.openlmis.order;

import org.openlmis.order.repository.OrderLineItemRepository;
import org.openlmis.order.repository.OrderRepository;
import org.openlmis.order.repository.ProofOfDeliveryLineItemRepository;
import org.openlmis.order.repository.ProofOfDeliveryRepository;
import org.openlmis.order.repository.RequisitionRepository;
import org.openlmis.order.repository.TemplateParameterRepository;
import org.openlmis.order.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CleanRepositoryHelper {

  public static final UUID INITIAL_USER_ID =
      UUID.fromString("35316636-6264-6331-2d34-3933322d3462");

  @Autowired
  private RequisitionRepository requisitionRepository;

  @Autowired
  private OrderLineItemRepository orderLineItemRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ProofOfDeliveryLineItemRepository proofOfDeliveryLineItemRepository;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private TemplateParameterRepository templateParameterRepository;

  @Autowired
  private TemplateRepository templateRepository;

  /**
   * Delete all entities from most of repositories.
   */
  @Transactional
  public void cleanAll() {
    templateParameterRepository.deleteAll();
    templateRepository.deleteAll();
    proofOfDeliveryLineItemRepository.deleteAll();
    proofOfDeliveryRepository.deleteAll();
    orderLineItemRepository.deleteAll();
    requisitionRepository.deleteAll();
    orderRepository.deleteAll();
  }
}
