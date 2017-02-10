package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.OrderNumberConfiguration;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.web.util.OrderNumberConfigurationDto;
import org.openlmis.fulfillment.repository.OrderNumberConfigurationRepository;
import org.openlmis.fulfillment.web.validator.OrderNumberConfigurationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Iterator;

import javax.validation.Valid;

@Controller
@Transactional
public class OrderNumberConfigurationController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFileTemplateController.class);


  @Autowired
  private OrderNumberConfigurationRepository orderNumberConfigurationRepository;

  @Autowired
  private OrderNumberConfigurationValidator validator;

  @Autowired
  private PermissionService permissionService;

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    binder.addValidators(validator);
  }

  /**
   * Saves given OrderNumberConfiguration to database.
   *
   * @param orderNumberConfigurationDto object to save.
   * @return Response entity with Http status code.
   */
  @RequestMapping(value = "/orderNumberConfigurations", method = RequestMethod.POST)
  public ResponseEntity<Object> saveOrderNumberConfigurations(
      @RequestBody @Valid OrderNumberConfigurationDto orderNumberConfigurationDto,
      BindingResult bindingResult) {

    LOGGER.debug("Checking right to update order number configuration");
    permissionService.canManageSystemSettings();

    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }

    OrderNumberConfiguration orderNumberConfiguration = OrderNumberConfiguration
        .newInstance(orderNumberConfigurationDto);

    Iterator<OrderNumberConfiguration> it = orderNumberConfigurationRepository.findAll().iterator();

    if (it.hasNext()) {
      orderNumberConfiguration.setId(it.next().getId());
    }

    OrderNumberConfiguration savedOrderNumberConfiguration =
        orderNumberConfigurationRepository.save(orderNumberConfiguration);

    OrderNumberConfigurationDto orderNumberConfigurationDto1 = OrderNumberConfigurationDto
        .newInstance(savedOrderNumberConfiguration);

    return new ResponseEntity<>(orderNumberConfigurationDto1, HttpStatus.OK);
  }

  /**
   * Get orderNumberConfiguration.
   *
   * @return OrderNumberConfiguration.
   */
  @RequestMapping(value = "/orderNumberConfigurations", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<OrderNumberConfigurationDto> getOrderFileTemplate() {

    LOGGER.debug("Checking right to view order number configuration");
    permissionService.canManageSystemSettings();

    Iterator<OrderNumberConfiguration> it = orderNumberConfigurationRepository.findAll().iterator();

    if (!it.hasNext()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    OrderNumberConfigurationDto orderNumberConfigurationDto = new OrderNumberConfigurationDto();
    it.next().export(orderNumberConfigurationDto);

    return new ResponseEntity<>(orderNumberConfigurationDto, HttpStatus.OK);
  }
}
