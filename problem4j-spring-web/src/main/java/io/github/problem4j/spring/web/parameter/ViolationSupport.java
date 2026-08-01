/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.problem4j.spring.web.parameter;

/**
 * Central constants used when constructing RFC 7807 problem responses for parameter and validation
 * errors.
 *
 * <p>Contains:
 *
 * <ul>
 *   <li>Standard detail message templates (e.g. missing / validation / type mismatch)
 *   <li>Source labels reported by Spring missing-value exceptions
 *   <li>Well-known extension key names added to problems
 *   <li>Generic fragments reused inside violations or fallback messages
 * </ul>
 *
 * @since 3.0.0
 */
public final class ViolationSupport {

  // ---------------------------------------------------------------------------
  // Detail message templates (surface-level, human-readable strings) used for
  // Problem.detail across various resolvers. Each corresponds to a specific
  // failure/validation scenario detected by Spring MVC / WebFlux or internal logic.
  // ---------------------------------------------------------------------------

  /**
   * Detail message for missing request parameter errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_REQUEST_PARAM_DETAIL = "Missing request param";

  /**
   * Detail message for missing request part errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_REQUEST_PART_DETAIL = "Missing request part";

  /**
   * Detail message for missing header errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_HEADER_DETAIL = "Missing header";

  /**
   * Detail message for missing path variable errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_PATH_VARIABLE_DETAIL = "Missing path variable";

  /**
   * Detail message for missing cookie errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_COOKIE_DETAIL = "Missing cookie";

  /**
   * Detail message for missing request attribute errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_REQUEST_ATTRIBUTE_DETAIL = "Missing request attribute";

  /**
   * Detail message for missing session attribute errors.
   *
   * @since 3.0.0
   */
  public static final String MISSING_SESSION_ATTRIBUTE_DETAIL = "Missing session attribute";

  /**
   * Detail message for type mismatch errors.
   *
   * @since 3.0.0
   */
  public static final String TYPE_MISMATCH_DETAIL = "Type mismatch";

  /**
   * Detail message for validation failed errors.
   *
   * @since 3.0.0
   */
  public static final String VALIDATION_FAILED_DETAIL = "Validation failed";

  /**
   * Detail message for max upload size exceeded errors.
   *
   * @since 3.0.0
   */
  public static final String MAX_UPLOAD_SIZE_EXCEEDED_DETAIL = "Max upload size exceeded";

  // ---------------------------------------------------------------------------
  // Source labels emitted by Spring (e.g. MissingRequestValueException#getLabel()) or used to
  // classify missing-value scenarios. Resolvers map these labels to the above detail templates
  // and complementary metadata extensions.
  // ---------------------------------------------------------------------------

  /**
   * Label for query parameter source.
   *
   * @since 3.0.0
   */
  public static final String QUERY_PARAMETER_LABEL = "query parameter";

  /**
   * Label for request part source.
   *
   * @since 3.0.0
   */
  public static final String REQUEST_PART_LABEL = "request part";

  /**
   * Label for header source.
   *
   * @since 3.0.0
   */
  public static final String HEADER_LABEL = "header";

  /**
   * Label for path parameter source.
   *
   * @since 3.0.0
   */
  public static final String PATH_PARAMETER_LABEL = "path parameter";

  /**
   * Label for cookie source.
   *
   * @since 3.0.0
   */
  public static final String COOKIE_LABEL = "cookie";

  /**
   * Label for request attribute source.
   *
   * @since 3.0.0
   */
  public static final String REQUEST_ATTRIBUTE_LABEL = "request attribute";

  /**
   * Label for session attribute source.
   *
   * @since 3.0.0
   */
  public static final String SESSION_ATTRIBUTE_LABEL = "session attribute";

  // ---------------------------------------------------------------------------
  // Extension keys injected into Problem.extension(...) to expose structured metadata to clients.
  // Naming is intentionally short & stable for JSON footprint and public contract clarity.
  // ---------------------------------------------------------------------------

  /**
   * Extension key for parameter metadata.
   *
   * @since 3.0.0
   */
  public static final String PARAM_EXTENSION = "param";

  /**
   * Extension key for kind metadata.
   *
   * @since 3.0.0
   */
  public static final String KIND_EXTENSION = "kind";

  /**
   * Extension key for header metadata.
   *
   * @since 3.0.0
   */
  public static final String HEADER_EXTENSION = "header";

  /**
   * Extension key for attribute metadata.
   *
   * @since 3.0.0
   */
  public static final String ATTRIBUTE_EXTENSION = "attribute";

  /**
   * Extension key for name metadata.
   *
   * @since 3.0.0
   */
  public static final String NAME_EXTENSION = "name";

  /**
   * Extension key for cookie metadata.
   *
   * @since 3.0.0
   */
  public static final String COOKIE_EXTENSION = "cookie";

  /**
   * Extension key for property metadata.
   *
   * @since 3.0.0
   */
  public static final String PROPERTY_EXTENSION = "property";

  /**
   * Extension key for errors metadata.
   *
   * @since 3.0.0
   */
  public static final String ERRORS_EXTENSION = "errors";

  /**
   * Extension key for max metadata.
   *
   * @since 3.0.0
   */
  public static final String MAX_EXTENSION = "max";

  // ---------------------------------------------------------------------------
  // Generic fragments reused inside violations or fallback messages.
  // ---------------------------------------------------------------------------

  /**
   * Generic error message for invalid values.
   *
   * @since 3.0.0
   */
  public static final String IS_NOT_VALID_ERROR = "is not valid";

  private ViolationSupport() {}
}
