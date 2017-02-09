package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.OrderFileTemplate;
import org.openlmis.fulfillment.service.PermissionService;
import org.openlmis.fulfillment.web.util.OrderFileTemplateDto;
import org.openlmis.fulfillment.repository.OrderFileTemplateRepository;
import org.openlmis.fulfillment.service.OrderFileTemplateService;
import org.openlmis.fulfillment.web.validator.OrderFileTemplateValidator;
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

import javax.validation.Valid;

@Controller
@Transactional
public class OrderFileTemplateController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderFileTemplateController.class);

  @Autowired
  private OrderFileTemplateValidator validator;
  @Autowired
  private OrderFileTemplateRepository orderFileTemplateRepository;
  @Autowired
  private OrderFileTemplateService orderFileTemplateService;
  @Autowired
  private PermissionService permissionService;

  @InitBinder
  protected void initBinder(WebDataBinder binder) {
    binder.setValidator(this.validator);
  }

  /**
   * Allows creating or updating orderFileTemplate.
   *
   * @param orderFileTemplateDto A orderFileTemplate bound to the request body
   * @return ResponseEntity containing saved orderFileTemplate
   */
  @RequestMapping(value = "/orderFileTemplates", method = RequestMethod.POST)
  public ResponseEntity<Object> savedOrderFileTemplate(
      @RequestBody @Valid OrderFileTemplateDto orderFileTemplateDto, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return new ResponseEntity<>(getErrors(bindingResult), HttpStatus.BAD_REQUEST);
    }

    LOGGER.debug("Checking right to update order file template");
    permissionService.canManageSystemSettings();


    LOGGER.debug("Saving Order File Template");
    OrderFileTemplate orderFileTemplate = OrderFileTemplate.newInstance(
        orderFileTemplateDto);

    OrderFileTemplate savedTemplate = orderFileTemplateRepository.save(orderFileTemplate);

    LOGGER.debug("Saved Order File Template with id: " + orderFileTemplate.getId());
    return new ResponseEntity<>( OrderFileTemplateDto.newInstance(savedTemplate),
        HttpStatus.CREATED);
  }

  /**
   * Get orderFileTemplate.
   *
   * @return OrderFileTemplate.
   */
  @RequestMapping(value = "/orderFileTemplates", method = RequestMethod.GET)
  public ResponseEntity<OrderFileTemplateDto> getOrderFileTemplate() {

    LOGGER.debug("Checking right to view order file template");
    permissionService.canManageSystemSettings();

    OrderFileTemplate orderFileTemplate = orderFileTemplateService.getOrderFileTemplate();
    if (orderFileTemplate == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(OrderFileTemplateDto.newInstance(orderFileTemplate),
          HttpStatus.OK);
    }
  }
}
