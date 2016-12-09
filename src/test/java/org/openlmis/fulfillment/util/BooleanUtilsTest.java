package org.openlmis.fulfillment.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Arrays;

public class BooleanUtilsTest {

  @Test
  public void shouldConvertBooleanValuesToBoolean() {
    isTrue(true);
    isFalse(false);
  }

  @Test
  public void shouldConvertStringValuesToBoolean() {
    isTrue("true", "TRUE", "TrUe");
    isFalse("false", "FALSE", "FaLsE");
    isFalse("", "   ", "abcde");
  }

  @Test
  public void shouldConvertNumberValuesToBoolean() {
    isTrue((byte) 1, (short) 10, 100, 1000L, 1.0, 10.13F);
    isFalse((byte) 0, (short) -10, -100, -1000L, -1.0, -10.13F);
  }

  @Test
  public void shouldConvertIncorrectValuesToBoolean() {
    isFalse(null, new Object());
  }

  private void isFalse(Object... values) {
    Arrays.stream(values).forEach(value -> check(false, value));
  }

  private void isTrue(Object... values) {
    Arrays.stream(values).forEach(value -> check(true, value));
  }

  private void check(boolean expected, Object value) {
    assertThat(BooleanUtils.toBoolean(value), is(expected));
  }

}
