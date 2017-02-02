package org.openlmis.fulfillment.domain;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.stream.Stream;

public class OrderStatusTest {

  @Test
  public void shouldFindStatus() throws Exception {
    Stream.of(OrderStatus.values())
        .forEach(val -> assertThat(OrderStatus.fromString(val.toString()), is(equalTo(val))));
    Stream.of(OrderStatus.values())
        .forEach(val -> assertThat(
            OrderStatus.fromString(val.toString().toLowerCase(ENGLISH)), is(equalTo(val))
        ));
    Stream.of(OrderStatus.values())
        .forEach(val -> assertThat(
            OrderStatus.fromString(val.toString().toUpperCase(ENGLISH)), is(equalTo(val))
        ));
  }

  @Test
  public void shouldNotFindStatus() throws Exception {
    assertThat(OrderStatus.fromString(null), is(nullValue()));
    assertThat(OrderStatus.fromString(""), is(nullValue()));
    assertThat(OrderStatus.fromString("     "), is(nullValue()));
    assertThat(OrderStatus.fromString(RandomStringUtils.random(10)), is(nullValue()));
  }
}
