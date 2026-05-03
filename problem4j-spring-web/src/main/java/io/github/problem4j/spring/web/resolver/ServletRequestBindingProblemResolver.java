/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.parameter.ViolationSupport.ATTRIBUTE_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.COOKIE_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.HEADER_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_COOKIE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_HEADER_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_PARAM_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_SESSION_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.NAME_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.PARAM_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;

/**
 * An {@link ProblemResolver} implementation that maps {@link ServletRequestBindingException} and
 * its common subtypes to {@link Problem} representations.
 *
 * <p>Each supported exception type is mapped to a {@link Problem} with {@code 400 Bad Request}
 * status and a human-readable detail message. Additional metadata about the missing element (such
 * as parameter name, header name, or attribute name) is added as extensions.
 *
 * @see MissingPathVariableException
 * @see MissingRequestCookieException
 * @see MissingRequestHeaderException
 * @see MissingServletRequestParameterException
 * @see ServletRequestBindingException
 * @since 1.2.0
 */
public class ServletRequestBindingProblemResolver extends AbstractProblemResolver {

  private static final Pattern MISSING_ATTRIBUTE_PATTERN =
      Pattern.compile("^Missing (session|request) attribute '([^']+)'");

  /**
   * Creates a new {@link ServletRequestBindingProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ServletRequestBindingProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link ServletRequestBindingProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public ServletRequestBindingProblemResolver(ProblemFormat problemFormat) {
    super(ServletRequestBindingException.class, problemFormat);
  }

  /**
   * Resolves a {@link ServletRequestBindingException} (or one of its common subclasses) into an
   * immutable {@link Problem} with {@link HttpStatus#BAD_REQUEST} status and appropriate detail
   * plus metadata extensions.
   *
   * <p>Subtype handling:
   *
   * <ul>
   *   <li>{@link MissingPathVariableException}: detail {@code MISSING_PATH_VARIABLE_DETAIL},
   *       extension {@code "name"}
   *   <li>{@link MissingServletRequestParameterException}: detail {@code
   *       MISSING_REQUEST_PARAM_DETAIL}, extensions {@code "param"}, {@code "kind"}
   *   <li>{@link MissingRequestHeaderException}: detail {@code MISSING_HEADER_DETAIL}, extension
   *       {@code "header"}
   *   <li>{@link MissingRequestCookieException}: detail {@code MISSING_COOKIE_DETAIL}, extension
   *       {@code "cookie"}
   *   <li>Generic {@link ServletRequestBindingException} whose message matches {@code ^Missing
   *       (session|request) attribute '...'}: detail set to corresponding missing attribute message
   *       and extension {@code attribute}
   * </ul>
   *
   * @param context problem context (unused)
   * @param ex binding-related exception to map
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; always BAD_REQUEST)
   * @return problem with status, detail, and extensions
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ProblemBuilder builder = Problem.builder().status(HttpStatus.BAD_REQUEST.value());

    if (ex instanceof MissingPathVariableException e) {
      builder =
          builder
              .detail(formatDetail(MISSING_PATH_VARIABLE_DETAIL))
              .extension(NAME_EXTENSION, e.getVariableName());
    } else if (ex instanceof MissingServletRequestParameterException e) {
      builder =
          builder
              .detail(formatDetail(MISSING_REQUEST_PARAM_DETAIL))
              .extension(PARAM_EXTENSION, e.getParameterName())
              .extension(KIND_EXTENSION, e.getParameterType().toLowerCase(Locale.ROOT));
    } else if (ex instanceof MissingRequestHeaderException e) {
      builder =
          builder
              .detail(formatDetail(MISSING_HEADER_DETAIL))
              .extension(HEADER_EXTENSION, e.getHeaderName());
    } else if (ex instanceof MissingRequestCookieException e) {
      builder =
          builder
              .detail(formatDetail(MISSING_COOKIE_DETAIL))
              .extension(COOKIE_EXTENSION, e.getCookieName());
    } else if (ex instanceof ServletRequestBindingException e) {
      Matcher matcher = MISSING_ATTRIBUTE_PATTERN.matcher(e.getMessage());
      if (matcher.find()) {
        String scope = matcher.group(1);
        String attribute = matcher.group(2);
        builder = extentAttributeDetail(scope, builder, attribute);
      }
    }

    return builder.build();
  }

  private ProblemBuilder extentAttributeDetail(
      String scope, ProblemBuilder builder, String attribute) {
    if (scope.equals("session")) {
      builder = builder.detail(formatDetail(MISSING_SESSION_ATTRIBUTE_DETAIL));
    } else {
      builder = builder.detail(formatDetail(MISSING_REQUEST_ATTRIBUTE_DETAIL));
    }
    return builder.extension(ATTRIBUTE_EXTENSION, attribute);
  }
}
