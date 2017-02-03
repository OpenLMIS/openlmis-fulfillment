package org.openlmis.fulfillment.i18n;

public abstract class MessageKeys {
  private static final String SERVICE_PREFIX = "fulfillment";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";
  private static final String VALIDATION_ERROR = SERVICE_PREFIX + ".validationError";

  public static final String ERROR_CLASS_NOT_FOUND = ERROR_PREFIX + ".class-not-found";
  public static final String ERROR_DATA_INTEGRITY_VIOLATION = ERROR_PREFIX
      + ".data-integrity-violation";
  public static final String ERROR_IO = ERROR_PREFIX + ".io";

  public static final String ERROR_CONFIGURATION_SETTING_NOT_FOUND = ERROR_PREFIX
      + ".configuration.setting-not-found";

  public static final String ERROR_JASPER = ERROR_PREFIX + ".jasper";
  public static final String ERROR_JASPER_FILE_CREATION = ERROR_PREFIX + ".jasper.file-creation";

  public static final String ERROR_PERMISSION_MISSING = ERROR_PREFIX + ".permission.missing";

  public static final String ERROR_ORDER_NOT_FOUND = ERROR_PREFIX + ".order.not-found";
  public static final String ERROR_ORDER_INCORRECT_STATUS = ERROR_PREFIX
      + ".order.incorrectStatus";
  public static final String ERROR_ORDER_INVALID_STATUS = ERROR_PREFIX + ".order.invalidStatus";

  public static final String ERROR_ORDER_RETRY_INVALID_STATUS = ERROR_PREFIX
      + ".order-retry.invalid-status";

  public static final String ERROR_REFERENCE_DATA_RETRIEVE = ERROR_PREFIX
      + ".reference-data.retrieve";

  public static final String ERROR_REPORTING_CREATION = ERROR_PREFIX + ".reporting.creation";
  public static final String ERROR_REPORTING_EXTRA_PROPERTIES = ERROR_PREFIX
      + ".reporting.extra-properties";
  public static final String ERROR_REPORTING_FILE_EMPTY = ERROR_PREFIX + ".reporting.file.empty";
  public static final String ERROR_REPORTING_FILE_INCORRECT_TYPE = ERROR_PREFIX
      + ".reporting.file.incorrect-type";
  public static final String ERROR_REPORTING_FILE_INVALID = ERROR_PREFIX
      + ".reporting.file.invalid";
  public static final String ERROR_REPORTING_FILE_MISSING = ERROR_PREFIX
      + ".reporting.file.missing";
  public static final String ERROR_REPORTING_PARAMETER_INCORRECT_TYPE = ERROR_PREFIX
      + ".reporting.parameter.incorrect-type";
  public static final String ERROR_REPORTING_PARAMETER_MISSING = ERROR_PREFIX
      + ".reporting.parameter.missing";
  public static final String ERROR_REPORTING_TEMPLATE_EXIST = ERROR_PREFIX
      + ".reporting.template.exist";

  public static final String ERROR_TRANSFER_PROPERTIES_DUPLICATE = ERROR_PREFIX
      + ".transfer-properties.duplicate";
  public static final String ERROR_TRANSFER_PROPERTIES_INCORRECT = ERROR_PREFIX
      + ".transfer-properties.incorrect";

  public static final String ERROR_PROOF_OF_DELIVERY_NOT_FOUND =
      ERROR_PREFIX + ".proofOfDelivery.notFound";
  public static final String ERROR_PROOF_OF_DELIVERY_ALREADY_SUBMITTED =
      ERROR_PREFIX + ".proofOfDelivery.alreadySubmitted";
  public static final String ERROR_PROOF_OD_DELIVERY_VALIDATION =
      ERROR_PREFIX + ".proofOdDelivery.validation";

  public static final String VALIDATION_ERROR_MUST_CONTAIN_VALUE =
      VALIDATION_ERROR + ".mustContainValue";
  public static final String VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO =
      VALIDATION_ERROR + ".mustBeGreaterThanOrEqualToZero";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

}
