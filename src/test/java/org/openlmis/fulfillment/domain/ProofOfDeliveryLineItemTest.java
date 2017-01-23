package org.openlmis.fulfillment.domain;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ProofOfDeliveryLineItemTest {

  @Test
  public void shouldCreateNewInstanceBasedOnParentAndOrderLineItem() {
    ProofOfDelivery pod = new ProofOfDelivery();
    OrderLineItem line = new OrderLineItem();

    ProofOfDeliveryLineItem item = new ProofOfDeliveryLineItem(pod, line);

    assertThat(item, is(notNullValue()));
    assertThat(item.getOrderLineItem(), is(line));
    assertThat(item.getProofOfDelivery(), is(pod));
  }
}
