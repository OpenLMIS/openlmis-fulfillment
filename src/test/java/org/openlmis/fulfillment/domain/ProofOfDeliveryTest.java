package org.openlmis.fulfillment.domain;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;

import org.junit.Test;

public class ProofOfDeliveryTest {

  @Test
  public void shouldCreateNewInstanceBasedOnOrder() throws Exception {
    Order order = new Order();
    order.setOrderLineItems(Lists.newArrayList(new OrderLineItem()));

    ProofOfDelivery pod = new ProofOfDelivery(order);

    assertThat(pod, is(notNullValue()));
    assertThat(pod.getOrder(), is(order));
    assertThat(pod.getProofOfDeliveryLineItems(), hasSize(1));
    assertThat(
        pod.getProofOfDeliveryLineItems().get(0).getOrderLineItem(),
        is(order.getOrderLineItems().get(0))
    );
  }
}
