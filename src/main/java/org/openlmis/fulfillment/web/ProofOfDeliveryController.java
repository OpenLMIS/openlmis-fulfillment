package org.openlmis.fulfillment.web;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.Template;
import org.openlmis.fulfillment.repository.ProofOfDeliveryRepository;
import org.openlmis.fulfillment.service.ExporterBuilder;
import org.openlmis.fulfillment.service.JasperReportViewException;
import org.openlmis.fulfillment.service.JasperReportsViewService;
import org.openlmis.fulfillment.service.TemplateService;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ReportUtils;
import org.openlmis.fulfillment.web.validator.ProofOfDeliveryValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsMultiFormatView;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Controller
public class ProofOfDeliveryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProofOfDeliveryController.class);
  private static final String PRINT_POD = "Print POD";

  @Autowired
  private JasperReportsViewService jasperReportsViewService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private ProofOfDeliveryRepository proofOfDeliveryRepository;

  @Autowired
  private ExporterBuilder exporter;

  @Autowired
  private ProofOfDeliveryValidator validator;

  @InitBinder
  protected void initBinder(final WebDataBinder binder) {
    binder.addValidators(validator);
  }

  /**
   * Allows creating new proofOfDeliveries.
   * If the id is specified, it will be ignored.
   *
   * @param pod A proofOfDelivery bound to the request body
   * @return ResponseEntity containing the created proofOfDelivery
   */
  @RequestMapping(value = "/proofOfDeliveries", method = RequestMethod.POST)
  public ResponseEntity createProofOfDelivery(@RequestBody @Valid ProofOfDeliveryDto pod,
                                              BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(getErrors(bindingResult));
    }

    LOGGER.debug("Creating new proofOfDelivery");
    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(pod);

    proofOfDelivery.setId(null);
    ProofOfDelivery newProofOfDelivery = proofOfDeliveryRepository.save(proofOfDelivery);

    LOGGER.debug("Created new proofOfDelivery with id: " + pod.getId());
    return new ResponseEntity<>(
        ProofOfDeliveryDto.newInstance(newProofOfDelivery,exporter),
        HttpStatus.CREATED
    );
  }

  /**
   * Get all proofOfDeliveries.
   *
   * @return ProofOfDeliveries.
   */
  @RequestMapping(value = "/proofOfDeliveries", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<Collection<ProofOfDeliveryDto>> getAllProofOfDeliveries() {
    Iterable<ProofOfDelivery> proofOfDeliveries = proofOfDeliveryRepository.findAll();
    return new ResponseEntity<>(
        ProofOfDeliveryDto.newInstance(proofOfDeliveries, exporter),
        HttpStatus.OK
    );
  }

  /**
   * Allows updating proofOfDeliveries.
   *
   * @param proofOfDeliveryId  UUID of proofOfDelivery which we want to update
   * @param dto A proofOfDeliveryDto bound to the request body
   * @return ResponseEntity containing the updated proofOfDelivery
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.PUT)
  public ResponseEntity updateProofOfDelivery(@PathVariable("id") UUID proofOfDeliveryId,
                                              @RequestBody @Valid ProofOfDeliveryDto dto,
                                              BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(getErrors(bindingResult));
    }

    ProofOfDelivery proofOfDelivery = ProofOfDelivery.newInstance(dto);
    ProofOfDelivery proofOfDeliveryToUpdate =
        proofOfDeliveryRepository.findOne(proofOfDeliveryId);
    if (proofOfDeliveryToUpdate == null) {
      proofOfDeliveryToUpdate = new ProofOfDelivery();
      LOGGER.debug("Creating new proofOfDelivery");
    } else {
      LOGGER.debug("Updating proofOfDelivery with id: " + proofOfDeliveryId);
    }

    proofOfDeliveryToUpdate.updateFrom(proofOfDelivery);
    proofOfDeliveryToUpdate = proofOfDeliveryRepository.save(proofOfDeliveryToUpdate);

    LOGGER.debug("Saved proofOfDelivery with id: " + proofOfDeliveryToUpdate.getId());
    return new ResponseEntity<>(
        ProofOfDeliveryDto.newInstance(proofOfDeliveryToUpdate, exporter),
        HttpStatus.OK
    );
  }

  /**
   * Get chosen proofOfDelivery.
   *
   * @param id UUID of proofOfDelivery whose we want to get
   * @return ProofOfDelivery.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.GET)
  public ResponseEntity<ProofOfDeliveryDto> getProofOfDelivery(@PathVariable("id") UUID id) {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(id);
    if (proofOfDelivery == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(
          ProofOfDeliveryDto.newInstance(proofOfDelivery, exporter),
          HttpStatus.OK
      );
    }
  }

  /**
   * Allows deleting proofOfDelivery.
   *
   * @param id UUID of proofOfDelivery whose we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteProofOfDelivery(@PathVariable("id") UUID id) {
    ProofOfDelivery proofOfDelivery = proofOfDeliveryRepository.findOne(id);
    if (proofOfDelivery == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      proofOfDeliveryRepository.delete(proofOfDelivery);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Print to PDF Proof of Delivery.
   *
   * @param id The UUID of the ProofOfDelivery to print
   * @return ResponseEntity with the "#200 OK" HTTP response status and Pdf file on success, or
   *         ResponseEntity containing the error description status.
   */
  @RequestMapping(value = "/proofOfDeliveries/{id}/print", method = RequestMethod.GET)
  @ResponseBody
  public ModelAndView print(HttpServletRequest request, @PathVariable("id") UUID id)
      throws JasperReportViewException {

    Template podPrintTemplate = templateService.getByName(PRINT_POD);

    Map<String, Object> params = ReportUtils.createParametersMap();
    String formatId = "'" + id + "'";
    params.put("pod_id", formatId);

    JasperReportsMultiFormatView jasperView =
        jasperReportsViewService.getJasperReportsView(podPrintTemplate, request);

    return new ModelAndView(jasperView, params);
  }
}
