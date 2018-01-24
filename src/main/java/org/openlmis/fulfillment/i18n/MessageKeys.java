/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.fulfillment.i18n;

import java.util.Arrays;

public abstract class MessageKeys {
  private static final String DELIMITER = ".";

  private static final String SERVICE_PREFIX = "fulfillment";
  private static final String SERVICE_ERROR_PREFIX = join(SERVICE_PREFIX, "error");
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";
  private static final String VALIDATION_ERROR = SERVICE_PREFIX + ".validationError";
  private static final String REQUIRED = "required";

  public static final String ERROR_USER_NOT_FOUND = join(SERVICE_PREFIX, "user", "notFound");

  public static final String ERROR_CLASS_NOT_FOUND = ERROR_PREFIX + ".class-not-found";
  public static final String ERROR_DATA_INTEGRITY_VIOLATION = ERROR_PREFIX
      + ".data-integrity-violation";
  public static final String ERROR_CONSTRAINT_VIOLATION = ERROR_PREFIX
      + ".constraintViolation";
  public static final String ERROR_IO = ERROR_PREFIX + ".io";
  public static final String ERROR_ENCODING =
      join(SERVICE_ERROR_PREFIX, "encoding", "notSupported");

  public static final String ERROR_JASPER = ERROR_PREFIX + ".jasper";
  public static final String ERROR_JASPER_FILE_CREATION = ERROR_PREFIX + ".jasper.file-creation";
  public static final String ERROR_JASPER_REPORT_CREATION_WITH_MESSAGE = ERROR_JASPER
      + ".reportCreation.with.message";

  public static final String ERROR_PERMISSION_MISSING = ERROR_PREFIX + ".permission.missing";

  public static final String ERROR_ORDER_NOT_FOUND = ERROR_PREFIX + ".order.not-found";
  public static final String ERROR_ORDER_INCORRECT_STATUS = ERROR_PREFIX
      + ".order.incorrectStatus";
  public static final String ERROR_ORDER_INVALID_STATUS = ERROR_PREFIX + ".order.invalidStatus";
  public static final String ERROR_ORDER_IN_USE = ERROR_PREFIX + ".order.orderInUse";

  public static final String ERROR_ORDER_RETRY_INVALID_STATUS = ERROR_PREFIX
      + ".order-retry.invalid-status";

  public static final String ERROR_ORDER_FILE_TEMPLATE_CREATE = ERROR_PREFIX
      + ".orderFileTemplate.create";

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
  public static final String ERROR_REPORTING_TEMPLATE_EXISTS = ERROR_PREFIX
      + ".reporting.template.exists";
  public static final String ERROR_REPORTING_TEMPLATE_NOT_FOUND_WITH_NAME = ERROR_PREFIX
      + ".reporting.template.notFound.with.name";

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
  public static final String ERROR_CANNOT_UPDATE_POD_BECAUSE_IT_WAS_SUBMITTED = ERROR_PREFIX
      + ".proofOfDelivery.cannotUpdateSubmitted";

  public static final String VALIDATION_ERROR_MUST_CONTAIN_VALUE =
      VALIDATION_ERROR + ".mustContainValue";
  public static final String VALIDATION_ERROR_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO =
      VALIDATION_ERROR + ".mustBeGreaterThanOrEqualToZero";

  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_SUBJECT
      = "fulfillment.email.orderCreation.subject";
  public static final String FULFILLMENT_EMAIL_ORDER_CREATION_BODY
      = "fulfillment.email.orderCreation.body";

  private static final String ERROR_DTO_EXPANSION = join(SERVICE_ERROR_PREFIX, "dtoExpansion");
  public static final String ERROR_DTO_EXPANSION_CAST = join(ERROR_DTO_EXPANSION, "cast");
  public static final String ERROR_DTO_EXPANSION_HREF = join(ERROR_DTO_EXPANSION, "href");
  public static final String ERROR_DTO_EXPANSION_ASSIGNMENT = join(ERROR_DTO_EXPANSION,
      "assignment");


  private static final String SHIPMENT = "shipment";
  public static final String SHIPMENT_NOT_FOUND =
      join(SERVICE_ERROR_PREFIX, SHIPMENT, "notFound");
  public static final String SHIPMENT_ORDERLESS_NOT_SUPPORTED =
      join(SERVICE_ERROR_PREFIX, SHIPMENT, "orderless", "notSupported");
  public static final String SHIPMENT_LINE_ITEMS_REQUIRED =
      join(SERVICE_ERROR_PREFIX, SHIPMENT, "lineItems", REQUIRED);
  public static final String SHIPMENT_ORDER_DUPLICATE =
      join(SERVICE_ERROR_PREFIX, SHIPMENT, "order", "duplicate");
  public static final String SHIPMENT_ORDER_REQUIRED =
      join(SERVICE_ERROR_PREFIX, SHIPMENT, "orderId", REQUIRED);

  private static final String SHIPMENT_DRAFT = "shipmentDraft";
  public static final String SHIPMENT_DRAFT_ORDER_REQUIRED =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "orderId", REQUIRED);
  public static final String SHIPMENT_DRAFT_ORDER_NOT_FOUND =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "order", "notFound");
  public static final String SHIPMENT_DRAFT_LINE_ITEMS_REQUIRED =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "lineItems", REQUIRED);
  public static final String SHIPMENT_DRAFT_ID_MISMATCH =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "id", "mismatch");
  public static final String SHIPMENT_DRAT_ORDER_DUPLICATE =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "order", "duplicate");
  public static final String CANNOT_CREATE_SHIPMENT_DRAFT_FOR_ORDER_WITH_WRONG_STATUS =
      join(SERVICE_ERROR_PREFIX, SHIPMENT_DRAFT, "create", "withWrongStatus");

  protected static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

}
