/*
 * Copyright (c) 2025-2026 Damian Malczewski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.problem4j.spring.web;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Central constants and small utilities used when constructing RFC 7807 {@link Problem} responses.
 *
 * <p>Contains:
 *
 * <ul>
 *   <li>Standard detail message templates (e.g. missing / validation / type mismatch)
 *   <li>Source labels reported by Spring missing-value exceptions
 *   <li>Well-known extension key names added to problems
 *   <li>Status resolution helpers bridging {@link Problem} and Spring HTTP status abstractions
 * </ul>
 *
 * Not intended for instantiation or external mutation.
 */
public final class ProblemSupport {

  // ---------------------------------------------------------------------------
  // Detail message templates (surface-level, human-readable strings) used for
  // Problem.detail across various resolvers. Each corresponds to a specific
  // failure/validation scenario detected by Spring MVC / WebFlux or internal logic.
  // ---------------------------------------------------------------------------

  /** Detail message for missing request parameter errors. */
  public static final String MISSING_REQUEST_PARAM_DETAIL = "Missing request param";

  /** Detail message for missing request part errors. */
  public static final String MISSING_REQUEST_PART_DETAIL = "Missing request part";

  /** Detail message for missing header errors. */
  public static final String MISSING_HEADER_DETAIL = "Missing header";

  /** Detail message for missing path variable errors. */
  public static final String MISSING_PATH_VARIABLE_DETAIL = "Missing path variable";

  /** Detail message for missing cookie errors. */
  public static final String MISSING_COOKIE_DETAIL = "Missing cookie";

  /** Detail message for missing request attribute errors. */
  public static final String MISSING_REQUEST_ATTRIBUTE_DETAIL = "Missing request attribute";

  /** Detail message for missing session attribute errors. */
  public static final String MISSING_SESSION_ATTRIBUTE_DETAIL = "Missing session attribute";

  /** Detail message for type mismatch errors. */
  public static final String TYPE_MISMATCH_DETAIL = "Type mismatch";

  /** Detail message for validation failed errors. */
  public static final String VALIDATION_FAILED_DETAIL = "Validation failed";

  /** Detail message for max upload size exceeded errors. */
  public static final String MAX_UPLOAD_SIZE_EXCEEDED_DETAIL = "Max upload size exceeded";

  // ---------------------------------------------------------------------------
  // Source labels emitted by Spring (e.g. MissingRequestValueException#getLabel()) or used to
  // classify missing-value scenarios. Resolvers map these labels to the above detail templates
  // and complementary metadata extensions.
  // ---------------------------------------------------------------------------

  /** Label for query parameter source. */
  public static final String QUERY_PARAMETER_LABEL = "query parameter";

  /** Label for request part source. */
  public static final String REQUEST_PART_LABEL = "request part";

  /** Label for header source. */
  public static final String HEADER_LABEL = "header";

  /** Label for path parameter source. */
  public static final String PATH_PARAMETER_LABEL = "path parameter";

  /** Label for cookie source. */
  public static final String COOKIE_LABEL = "cookie";

  /** Label for request attribute source. */
  public static final String REQUEST_ATTRIBUTE_LABEL = "request attribute";

  /** Label for session attribute source. */
  public static final String SESSION_ATTRIBUTE_LABEL = "session attribute";

  // ---------------------------------------------------------------------------
  // Extension keys injected into Problem.extension(...) to expose structured metadata to clients.
  // Naming is intentionally short & stable for JSON footprint and public contract clarity.
  // ---------------------------------------------------------------------------

  /** Extension key for parameter metadata. */
  public static final String PARAM_EXTENSION = "param";

  /** Extension key for kind metadata. */
  public static final String KIND_EXTENSION = "kind";

  /** Extension key for header metadata. */
  public static final String HEADER_EXTENSION = "header";

  /** Extension key for attribute metadata. */
  public static final String ATTRIBUTE_EXTENSION = "attribute";

  /** Extension key for name metadata. */
  public static final String NAME_EXTENSION = "name";

  /** Extension key for cookie metadata. */
  public static final String COOKIE_EXTENSION = "cookie";

  /** Extension key for property metadata. */
  public static final String PROPERTY_EXTENSION = "property";

  /** Extension key for errors metadata. */
  public static final String ERRORS_EXTENSION = "errors";

  /** Extension key for max metadata. */
  public static final String MAX_EXTENSION = "max";

  // ---------------------------------------------------------------------------
  // Generic fragments reused inside violations or fallback messages.
  // ---------------------------------------------------------------------------

  /** Generic error message for invalid values. */
  public static final String IS_NOT_VALID_ERROR = "is not valid";

  /**
   * Resolves a {@link Problem} to a corresponding {@link HttpStatus}.
   *
   * <p>If the problem's status value is not a valid HTTP status code, {@link
   * HttpStatus#INTERNAL_SERVER_ERROR} is returned as a fallback.
   *
   * @param problem the {@link Problem} instance containing the status code
   * @return the corresponding {@link HttpStatus}, or {@link HttpStatus#INTERNAL_SERVER_ERROR} if
   *     the status is invalid
   * @see HttpStatus#INTERNAL_SERVER_ERROR
   */
  public static HttpStatus resolveStatus(Problem problem) {
    try {
      return HttpStatus.valueOf(problem.getStatus());
    } catch (IllegalArgumentException e) {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  /**
   * Resolves an {@link HttpStatusCode} to a corresponding {@link ProblemStatus}.
   *
   * <p>If the status code is {@code null} or not recognized, {@link
   * ProblemStatus#INTERNAL_SERVER_ERROR} is returned as a fallback.
   *
   * @param status the {@link HttpStatusCode} to resolve
   * @return the corresponding {@link ProblemStatus}, or {@link ProblemStatus#INTERNAL_SERVER_ERROR}
   *     if the status is {@code null} or invalid
   */
  public static ProblemStatus resolveStatus(HttpStatusCode status) {
    return status == null
        ? ProblemStatus.INTERNAL_SERVER_ERROR
        : ProblemStatus.findValue(status.value()).orElse(ProblemStatus.INTERNAL_SERVER_ERROR);
  }

  private ProblemSupport() {}
}
