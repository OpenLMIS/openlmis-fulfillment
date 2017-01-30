package org.openlmis.fulfillment.web.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.PROOF_OF_DELIVERY_LINE_ITEMS;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;
import static org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem.QUANTITY_RECEIVED;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import com.google.common.collect.Lists;

import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.util.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProofOfDeliveryValidator {

  @Autowired
  private MessageService messageService;

  /**
   * Valides the given proof of delivery.
   *
   * @param target instance of {@link ProofOfDelivery} that should be validated.
   */
  public List<Message.LocalizedMessage> validate(ProofOfDelivery target) {
    List<Message.LocalizedMessage> errors = Lists.newArrayList();

    rejectIfBlank(errors, target.getDeliveredBy(), DELIVERED_BY);
    rejectIfBlank(errors, target.getReceivedBy(), RECEIVED_BY);
    rejectIfNull(errors, target.getReceivedDate(), RECEIVED_DATE);

    Optional.ofNullable(target.getProofOfDeliveryLineItems())
        .ifPresent(list -> list.forEach(line -> validateLine(line, errors)));

    return errors;
  }

  private void validateLine(ProofOfDeliveryLineItem line, List<Message.LocalizedMessage> errors) {
    rejectIfLessThanZero(errors, line.getQuantityReceived(), getLineField(QUANTITY_RECEIVED));
  }

  private void rejectIfLessThanZero(List<Message.LocalizedMessage> errors, Number value,
                                    String field) {
    if (null == value) {
      errors.add(getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    } else if (value.doubleValue() < 0) {
      errors.add(getErrorMessage(VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO, field));
    }
  }

  private void rejectIfNull(List<Message.LocalizedMessage> errors, Object value, String field) {
    if (null == value) {
      errors.add(getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    }
  }

  private void rejectIfBlank(List<Message.LocalizedMessage> errors, String value, String field) {
    if (isBlank(value)) {
      errors.add(getErrorMessage(VALIDATION_ERROR_MUST_CONTAIN_VALUE, field));
    }
  }

  private String getLineField(String field) {
    return PROOF_OF_DELIVERY_LINE_ITEMS + '.' + field;
  }

  private Message.LocalizedMessage getErrorMessage(String key, Object... params) {
    return messageService.localize(new Message(key, params));
  }

}
