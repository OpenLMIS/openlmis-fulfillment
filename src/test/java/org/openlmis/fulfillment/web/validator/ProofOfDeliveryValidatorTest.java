package org.openlmis.fulfillment.web.validator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.PROOF_OF_DELIVERY_LINE_ITEMS;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;
import static org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem.QUANTITY_RECEIVED;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.util.Message;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryDto;
import org.openlmis.fulfillment.web.util.ProofOfDeliveryLineItemDto;
import org.springframework.validation.Errors;

import java.time.LocalDate;

@RunWith(MockitoJUnitRunner.class)
public class ProofOfDeliveryValidatorTest {
  private static final String MUST_CONTAIN_A_VALUE = "must contain a value";
  private static final String MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO =
      "must contain a value that is greater than or equal to zero";

  @Mock
  private MessageService messageService;

  @InjectMocks
  private ProofOfDeliveryValidator validator;

  @Mock
  private Errors errors;

  private ProofOfDeliveryDto pod;
  private ProofOfDeliveryLineItemDto line;

  @Before
  public void setUp() throws Exception {
    line = new ProofOfDeliveryLineItemDto();
    line.setQuantityReceived(10L);

    pod = new ProofOfDeliveryDto();
    pod.setDeliveredBy("Deliver guy");
    pod.setReceivedBy("Receiver");
    pod.setReceivedDate(LocalDate.now());
    pod.setProofOfDeliveryLineItems(Lists.newArrayList(line));

    mockLocalizedErrorMessage(
        new Message(VALIDATION_ERROR_MUST_CONTAIN_VALUE, DELIVERED_BY),
        MUST_CONTAIN_A_VALUE
    );
    mockLocalizedErrorMessage(
        new Message(VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_BY),
        MUST_CONTAIN_A_VALUE
    );
    mockLocalizedErrorMessage(
        new Message(VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_DATE),
        MUST_CONTAIN_A_VALUE
    );
    mockLocalizedErrorMessage(
        new Message(
            VALIDATION_ERROR_MUST_CONTAIN_VALUE,
            PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED
        ),
        MUST_CONTAIN_A_VALUE
    );
    mockLocalizedErrorMessage(new Message(
            VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO,
            PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED
        ),
        MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO
    );
  }

  @Test
  public void shouldSupportOnlyProofOfDelivery() throws Exception {
    assertThat(validator.supports(ProofOfDeliveryDto.class), is(true));
  }

  @Test
  public void shouldRejectIfDeliveredByIsBlank() throws Exception {
    pod.setDeliveredBy(null);
    validator.validate(pod, errors);

    pod.setDeliveredBy("");
    validator.validate(pod, errors);

    pod.setDeliveredBy("    ");
    validator.validate(pod, errors);

    verifyError(3, DELIVERED_BY, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfReceivedByIsBlank() throws Exception {
    pod.setReceivedBy(null);
    validator.validate(pod, errors);

    pod.setReceivedBy("");
    validator.validate(pod, errors);

    pod.setReceivedBy("      ");
    validator.validate(pod, errors);

    verifyError(3, RECEIVED_BY, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfDeliveredDateIsNull() throws Exception {
    pod.setReceivedDate(null);
    validator.validate(pod, errors);

    verifyError(RECEIVED_DATE, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsNull() throws Exception {
    line.setQuantityReceived(null);
    validator.validate(pod, errors);

    verifyError(
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE
    );
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsLessThanZero() throws Exception {
    line.setQuantityReceived(-5L);
    validator.validate(pod, errors);

    verifyError(
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO
    );
  }

  @Test
  public void shouldValidate() throws Exception {
    validator.validate(pod, errors);

    verifyZeroInteractions(errors);
  }

  private void verifyError(String field, String error) {
    verifyError(1, field, error);
  }

  private void verifyError(int times, String field, String error) {
    verify(errors, times(times)).rejectValue(eq(field), contains(error));
  }

  private void mockLocalizedErrorMessage(Message message, String error) {
    when(messageService.localize(message)).thenReturn(message.new LocalizedMessage(error));
  }
}
