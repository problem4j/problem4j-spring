/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.ProblemSupport.ATTRIBUTE_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.COOKIE_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.COOKIE_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.HEADER_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.HEADER_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_COOKIE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_HEADER_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_PARAM_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_REQUEST_PART_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.MISSING_SESSION_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.NAME_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PARAM_EXTENSION;
import static io.github.problem4j.spring.web.ProblemSupport.PATH_PARAMETER_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.QUERY_PARAMETER_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.REQUEST_ATTRIBUTE_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.REQUEST_PART_LABEL;
import static io.github.problem4j.spring.web.ProblemSupport.SESSION_ATTRIBUTE_LABEL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.MissingRequestValueException;

/**
 * Handles {@link MissingRequestValueException} thrown when a required request value is missing.
 *
 * <p>This typically occurs when a controller method expects a path variable, request parameter, or
 * header annotated with {@code @RequestParam}, {@code @PathVariable}, or {@code @RequestHeader},
 * but the client did not provide it.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 400 (Bad Request) response to
 * indicate that a required input value is missing.
 */
public class MissingRequestValueProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link MissingRequestValueProblemResolver} with default problem format. */
  public MissingRequestValueProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link MissingRequestValueProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public MissingRequestValueProblemResolver(ProblemFormat problemFormat) {
    super(MissingRequestValueException.class, problemFormat);
  }

  /**
   * Builds a {@link ProblemBuilder} for a {@link MissingRequestValueException}. Chooses a specific
   * detail message and sets identifying extensions based on {@link
   * MissingRequestValueException#getLabel()}. Falls back to a bare BAD_REQUEST if the label is
   * {@code null}.
   *
   * @param context problem context (unused)
   * @param ex the thrown {@link MissingRequestValueException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return builder populated with status, detail, and relevant extensions
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MissingRequestValueException e = (MissingRequestValueException) ex;

    ProblemBuilder builder = Problem.builder().status(ProblemStatus.BAD_REQUEST);

    if (e.getLabel() == null) {
      return builder;
    }

    switch (e.getLabel()) {
      case QUERY_PARAMETER_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_REQUEST_PARAM_DETAIL))
                  .extension(PARAM_EXTENSION, e.getName())
                  .extension(KIND_EXTENSION, e.getType().getSimpleName().toLowerCase(Locale.ROOT));
      case REQUEST_PART_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_REQUEST_PART_DETAIL))
                  .extension(PARAM_EXTENSION, e.getName());
      case HEADER_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_HEADER_DETAIL))
                  .extension(HEADER_EXTENSION, e.getName());
      case PATH_PARAMETER_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_PATH_VARIABLE_DETAIL))
                  .extension(NAME_EXTENSION, e.getName());
      case COOKIE_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_COOKIE_DETAIL))
                  .extension(COOKIE_EXTENSION, e.getName());
      case REQUEST_ATTRIBUTE_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_REQUEST_ATTRIBUTE_DETAIL))
                  .extension(ATTRIBUTE_EXTENSION, e.getName());
      case SESSION_ATTRIBUTE_LABEL ->
          builder =
              builder
                  .detail(formatDetail(MISSING_SESSION_ATTRIBUTE_DETAIL))
                  .extension(ATTRIBUTE_EXTENSION, e.getName());
    }

    return builder;
  }
}
