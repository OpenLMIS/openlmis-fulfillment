package org.openlmis.fulfillment.web.validator;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.PROOF_OF_DELIVERY_LINE_ITEMS;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;
import static org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem.QUANTITY_RECEIVED;
import static org.openlmis.fulfillment.i18n.MessageKeys.ERROR_PROOF_OD_DELIVERY_VALIDATION;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.util.Message;

import java.time.LocalDate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class ProofOfDeliveryValidatorTest {
  private static final String MUST_CONTAIN_A_VALUE = "must contain a value";
  private static final String MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO =
      "must contain a value that is greater than or equal to zero";

  private static final String MESSAGE_KEY = "messageKey";
  private static final String DETAILS = "details";
  private static final String FIELD = "field";
  private static final String ERROR = "error";

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private MessageService messageService;

  @InjectMocks
  private ProofOfDeliveryValidator validator;

  private ProofOfDelivery pod;
  private ProofOfDeliveryLineItem line;

  @Before
  public void setUp() throws Exception {
    line = new ProofOfDeliveryLineItem();
    line.setQuantityReceived(10L);

    pod = new ProofOfDelivery();
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
  public void shouldRejectIfDeliveredByIsNull() throws Exception {
    expectValidationException(DELIVERED_BY, MUST_CONTAIN_A_VALUE);

    pod.setDeliveredBy(null);
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfDeliveredByIsEmpty() throws Exception {
    expectValidationException(DELIVERED_BY, MUST_CONTAIN_A_VALUE);

    pod.setDeliveredBy("");
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfDeliveredByHasOnlyWhitespaces() throws Exception {
    expectValidationException(DELIVERED_BY, MUST_CONTAIN_A_VALUE);

    pod.setDeliveredBy("     ");
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfReceivedByIsNull() throws Exception {
    expectValidationException(RECEIVED_BY, MUST_CONTAIN_A_VALUE);

    pod.setReceivedBy(null);
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfReceivedByIsEmpty() throws Exception {
    expectValidationException(RECEIVED_BY, MUST_CONTAIN_A_VALUE);

    pod.setReceivedBy("");
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfReceivedByHasOnlyWhitespaces() throws Exception {
    expectValidationException(RECEIVED_BY, MUST_CONTAIN_A_VALUE);

    pod.setReceivedBy("     ");
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfDeliveredDateIsNull() throws Exception {
    expectValidationException(RECEIVED_DATE, MUST_CONTAIN_A_VALUE);

    pod.setReceivedDate(null);
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsNull() throws Exception {
    expectValidationException(
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE
    );

    line.setQuantityReceived(null);
    validator.validate(pod);
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsLessThanZero() throws Exception {
    expectValidationException(
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO
    );

    line.setQuantityReceived(-5L);
    validator.validate(pod);
  }

  @Test
  public void shouldValidate() throws Exception {
    expected = ExpectedException.none();

    validator.validate(pod);
  }

  private void expectValidationException(String field, String error) {
    expected.expect(ValidationException.class);
    expected.expect(allOf(
        hasProperty(MESSAGE_KEY, equalTo(ERROR_PROOF_OD_DELIVERY_VALIDATION)),
        hasProperty(DETAILS, hasItem(
            allOf(
                hasProperty(FIELD, equalTo(field)),
                hasProperty(ERROR, hasToString(containsString(error)))
            )
        ))
    ));
  }

  private void mockLocalizedErrorMessage(Message message, String error) {
    when(messageService.localize(message)).thenReturn(message.new LocalizedMessage(error));
  }
}
