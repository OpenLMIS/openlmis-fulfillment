package org.openlmis.fulfillment.web.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.PROOF_OF_DELIVERY_LINE_ITEMS;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;
import static org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem.QUANTITY_RECEIVED;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class ProofOfDeliveryValidator implements Validator {

  @Autowired
  private MessageService messageService;

  @Override
  public boolean supports(Class<?> clazz) {
    return ProofOfDeliveryDto.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    ProofOfDeliveryDto pod = (ProofOfDeliveryDto) target;

    rejectIfBlank(errors, pod.getDeliveredBy(), DELIVERED_BY);
    rejectIfBlank(errors, pod.getReceivedBy(), RECEIVED_BY);
    rejectIfNull(errors, pod.getReceivedDate(), RECEIVED_DATE);

    Optional.ofNullable(pod.getProofOfDeliveryLineItems())
        .ifPresent(list -> list.forEach(line -> validateLine(line, errors)));
  }

  private void validateLine(ProofOfDeliveryLineItem.Importer line, Errors errors) {
    rejectIfLessThanZero(errors, line.getQuantityReceived(), getLineField(QUANTITY_RECEIVED));
  }

  private void rejectIfLessThanZero(Errors errors, Number value, String field) {
    if (null == value) {
      errors.rejectValue(field, getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    } else if (value.doubleValue() < 0) {
      errors.rejectValue(
          field, getErrorMessage(VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO, field)
      );
    }
  }

  private void rejectIfNull(Errors errors, Object value, String field) {
    if (null == value) {
      errors.rejectValue(field, getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    }
  }

  private void rejectIfBlank(Errors errors, String value, String field) {
    if (isBlank(value)) {
      errors.rejectValue(field, getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    }
  }

  private String getLineField(String field) {
    return PROOF_OF_DELIVERY_LINE_ITEMS + '.' + field;
  }

  private String getErrorMessage(String key, String... params) {
    return messageService.localize(new Message(key, params)).toString();
  }
}