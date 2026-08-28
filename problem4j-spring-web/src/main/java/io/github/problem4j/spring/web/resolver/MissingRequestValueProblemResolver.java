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

package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.parameter.ViolationSupport.ATTRIBUTE_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.COOKIE_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.COOKIE_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.HEADER_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.HEADER_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.KIND_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_COOKIE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_HEADER_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_PARAM_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_REQUEST_PART_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_SESSION_ATTRIBUTE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.NAME_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.PARAM_EXTENSION;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.PATH_PARAMETER_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.QUERY_PARAMETER_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.REQUEST_ATTRIBUTE_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.REQUEST_PART_LABEL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.SESSION_ATTRIBUTE_LABEL;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import java.util.Locale;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 *
 * @since 1.2.0
 */
public class MissingRequestValueProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link MissingRequestValueProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public MissingRequestValueProblemResolver() {
    super(MissingRequestValueException.class);
  }

  /**
   * Creates a new {@link MissingRequestValueProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   * @deprecated since 3.1.0 as {@link
   *     io.github.problem4j.spring.web.config.ProblemBeanPostProcessor ProblemBeanPostProcessor}
   *     now assigns the {@link ProblemFormat} after construction; use {@link
   *     #MissingRequestValueProblemResolver()}
   */
  @SuppressWarnings("removal")
  @Deprecated(since = "3.1.0", forRemoval = true)
  public MissingRequestValueProblemResolver(ProblemFormat problemFormat) {
    super(MissingRequestValueException.class, problemFormat);
  }

  /**
   * Returns a {@link Problem} for a {@link MissingRequestValueException}. Chooses a specific detail
   * message and sets identifying extensions based on {@link
   * MissingRequestValueException#getLabel()}. Falls back to a bare BAD_REQUEST if the label is
   * {@code null}.
   *
   * @param context problem context (unused)
   * @param ex the thrown {@link MissingRequestValueException}
   * @param headers HTTP headers (unused)
   * @param status suggested status (ignored; BAD_REQUEST enforced)
   * @return problem with status, detail, and relevant extensions
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    MissingRequestValueException e = (MissingRequestValueException) ex;

    ProblemBuilder builder = Problem.builder().status(HttpStatus.BAD_REQUEST.value());

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

    return builder.build();
  }
}
