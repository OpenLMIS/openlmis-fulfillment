package org.openlmis.fulfillment.web.validator;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.DELIVERED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.PROOF_OF_DELIVERY_LINE_ITEMS;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_BY;
import static org.openlmis.fulfillment.domain.ProofOfDelivery.RECEIVED_DATE;
import static org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem.QUANTITY_RECEIVED;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO;
import static org.openlmis.fulfillment.i18n.MessageKeys.VALIDATION_ERROR_MUST_CONTAIN_VALUE;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.fulfillment.domain.ProofOfDelivery;
import org.openlmis.fulfillment.domain.ProofOfDeliveryLineItem;
import org.openlmis.fulfillment.i18n.ExposedMessageSource;
import org.openlmis.fulfillment.i18n.MessageService;
import org.openlmis.fulfillment.util.Message;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class ProofOfDeliveryValidatorTest {
  private static final String MUST_CONTAIN_A_VALUE = "must contain a value";
  private static final String MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO =
      "must contain a value that is greater than or equal to zero";

  private static final String MESSAGE_KEY = "messageKey";
  private static final String MESSAGE = "message";
  private static final String PARAMS = "params";

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Mock
  private ExposedMessageSource messageSource;

  @Spy
  private MessageService messageService;

  @InjectMocks
  private ProofOfDeliveryValidator validator;

  private ProofOfDelivery pod;
  private ProofOfDeliveryLineItem line;

  @Before
  public void setUp() throws Exception {
    FieldUtils.writeField(messageService, "messageSource", messageSource, true);

    line = new ProofOfDeliveryLineItem();
    line.setQuantityReceived(10L);

    pod = new ProofOfDelivery();
    pod.setDeliveredBy("Deliver guy");
    pod.setReceivedBy("Receiver");
    pod.setReceivedDate(ZonedDateTime.now());
    pod.setProofOfDeliveryLineItems(Lists.newArrayList(line));

    mockMessageSource(VALIDATION_ERROR_MUST_CONTAIN_VALUE, DELIVERED_BY, MUST_CONTAIN_A_VALUE);
    mockMessageSource(VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_BY, MUST_CONTAIN_A_VALUE);
    mockMessageSource(VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_DATE, MUST_CONTAIN_A_VALUE);
    mockMessageSource(
        VALIDATION_ERROR_MUST_CONTAIN_VALUE,
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE
    );
    mockMessageSource(
        VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO,
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO
    );
  }

  @Test
  public void shouldRejectIfDeliveredByIsBlank() throws Exception {
    pod.setDeliveredBy(null);

    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, DELIVERED_BY, MUST_CONTAIN_A_VALUE);

    pod.setDeliveredBy("");

    errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, DELIVERED_BY, MUST_CONTAIN_A_VALUE);

    pod.setDeliveredBy("     ");

    errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, DELIVERED_BY, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfReceivedByIsBlank() throws Exception {
    pod.setReceivedBy(null);

    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_BY, MUST_CONTAIN_A_VALUE);

    pod.setReceivedBy("");

    errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_BY, MUST_CONTAIN_A_VALUE);

    pod.setReceivedBy("     ");

    errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_BY, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfDeliveredDateIsNull() throws Exception {
    pod.setReceivedDate(null);

    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE, RECEIVED_DATE, MUST_CONTAIN_A_VALUE);
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsNull() throws Exception {
    line.setQuantityReceived(null);

    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_CONTAIN_VALUE,
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED, MUST_CONTAIN_A_VALUE
    );
  }

  @Test
  public void shouldRejectIfQuantityReceivedIsLessThanZero() throws Exception {
    line.setQuantityReceived(-5L);

    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertErrors(errors, VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO,
        PROOF_OF_DELIVERY_LINE_ITEMS + '.' + QUANTITY_RECEIVED,
        MUST_CONTAIN_A_VALUE_THAT_IS_GREATER_THAN_OR_EQUAL_TO_ZERO
    );
  }

  @Test
  public void shouldValidate() throws Exception {
    List<Message.LocalizedMessage> errors = validator.validate(pod);
    assertThat(errors, hasSize(0));
  }

  private void assertErrors(List<Message.LocalizedMessage> errors, String messageKey,
                            String field, String error) {
    assertThat(errors, hasItem(
        allOf(
            hasProperty(MESSAGE_KEY, equalTo(messageKey)),
            hasProperty(PARAMS, arrayContaining(equalTo(field))),
            hasProperty(MESSAGE, equalTo(error))
        )
    ));
  }

  private void mockMessageSource(String messageKey, String param, String message) {
    when(messageSource.getMessage(messageKey, new Object[]{param}, Locale.getDefault()))
        .thenReturn(message);
  }

}
