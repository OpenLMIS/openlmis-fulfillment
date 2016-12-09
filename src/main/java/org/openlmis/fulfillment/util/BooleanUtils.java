package org.openlmis.fulfillment.util;

public class BooleanUtils {

  private BooleanUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Tries to convert the given object value to {@link Boolean}.
   *
   * @param value any kind of object
   * @return true if object can be mapped to true value; otherwise false.
   */
  public static Boolean toBoolean(Object value) {
    if (value instanceof Boolean) {
      return (Boolean) value;
    }

    if (value instanceof String) {
      return Boolean.parseBoolean((String) value);
    }

    if (value instanceof Number) {
      return ((Number) value).longValue() > 0;
    }

    return false;
  }
}
