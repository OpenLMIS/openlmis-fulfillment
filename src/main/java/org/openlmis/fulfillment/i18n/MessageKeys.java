package org.openlmis.fulfillment.i18n;

public abstract class MessageKeys {
  private static final String PREFIX = "fulfillment";
  private static final String ERROR_PREFIX = PREFIX + ".error";

  public static final String ERROR_CLASS_NOT_FOUND = ERROR_PREFIX + ".class.not.found";
  public static final String ERROR_CONFIGURATION_SETTING_NOT_FOUND = ERROR_PREFIX
      + ".configuration.setting.not.found";
  public static final String ERROR_DATA_INTEGRITY_VIOLATION = ERROR_PREFIX
      + ".data.integrity.violation";
  public static final String ERROR_DUPLICATE_TRANSFER_PROPERTIES = ERROR_PREFIX
      + ".duplicate.transfer.properties";
  public static final String ERROR_INCORRECT_TRANSFER_PROPERTIES = ERROR_PREFIX
      + ".incorrect.transfer.properties";
  public static final String ERROR_IO = ERROR_PREFIX + ".io";
  public static final String ERROR_JASPER_ERROR = ERROR_PREFIX + ".jasper.error";
  public static final String ERROR_JASPER_FILE_COULD_NOT_BE_CREATED = ERROR_PREFIX
      + ".jasper.file.could.not.be.created";
  public static final String ERROR_MISSING_PERMISSION = ERROR_PREFIX + ".missing.permission";
  public static final String ERROR_REFERENCE_DATA_ERROR = ERROR_PREFIX + ".reference.data.error";
  public static final String ERROR_REPORTING_EMPTY_FILE = ERROR_PREFIX + ".reporting.empty.file";
  public static final String ERROR_REPORTING_ERROR_CREATING_REPORT = ERROR_PREFIX
      + ".reporting.error.creating.report";
  public static final String ERROR_REPORTING_EXTRA_PROPERTIES = ERROR_PREFIX
      + ".reporting.extra.properties";
  public static final String ERROR_REPORTING_INCORRECT_FILE_TYPE = ERROR_PREFIX
      + ".reporting.incorrect.file.type";
  public static final String ERROR_REPORTING_INCORRECT_PARAMETER_TYPE = ERROR_PREFIX
      + ".reporting.incorrect.parameter.type";
  public static final String ERROR_REPORTING_INVALID_FILE = ERROR_PREFIX
      + ".reporting.invalid.file";
  public static final String ERROR_REPORTING_MISSING_FILE = ERROR_PREFIX
      + ".reporting.missing.file";
  public static final String ERROR_REPORTING_MISSING_PARAMETER = ERROR_PREFIX
      + ".reporting.missing.parameter";
  public static final String ERROR_REPORTING_TEMPLATE_ALREDY_EXIST = ERROR_PREFIX
      + ".reporting.template.alredy.exist";

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }
}
