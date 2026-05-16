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

import static io.github.problem4j.spring.web.parameter.ViolationSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.parameter.ViolationSupport.NAME_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemFormat;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerErrorException;

/**
 * Handles {@link ServerErrorException} thrown when a server-side error occurs while processing a
 * request.
 *
 * <p>This exception indicates that an unexpected condition or internal failure prevented the server
 * from fulfilling the request.
 *
 * <p>The handler is responsible for returning an appropriate HTTP 5xx response (e.g., 500 Internal
 * Server Error) to inform the client that the request could not be processed due to a server-side
 * problem.
 *
 * @since 1.2.0
 */
public class ServerErrorProblemResolver extends AbstractProblemResolver {

  /**
   * Creates a new {@link ServerErrorProblemResolver} with default problem format.
   *
   * @since 1.2.0
   */
  public ServerErrorProblemResolver() {
    this(ProblemFormat.identity());
  }

  /**
   * Creates a new {@link ServerErrorProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   * @since 1.2.0
   */
  public ServerErrorProblemResolver(ProblemFormat problemFormat) {
    super(ServerErrorException.class, problemFormat);
  }

  /**
   * Resolves a {@link ServerErrorException} into an immutable {@link Problem}.
   *
   * <p>Special case: Spring WebFlux's {@code PathVariableMethodArgumentResolver} raises {@link
   * ServerErrorException} (instead of a missing-value exception) when a required {@code
   * PathVariable} is absent. In that scenario this method returns a BAD_REQUEST problem with a
   * standardized detail ({@code ViolationSupport#MISSING_PATH_VARIABLE_DETAIL}) and an extension
   * "{@code ViolationSupport#NAME_EXTENSION}" containing the variable name.
   *
   * <p>Otherwise, it falls back to a generic INTERNAL_SERVER_ERROR problem.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link ServerErrorException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; derives from condition)
   * @return problem with BAD_REQUEST + path variable info or INTERNAL_SERVER_ERROR
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport#MISSING_PATH_VARIABLE_DETAIL
   * @see io.github.problem4j.spring.web.parameter.ViolationSupport#NAME_EXTENSION
   * @see org.springframework.web.bind.annotation.PathVariable
   * @since 3.0.0
   */
  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ServerErrorException e = (ServerErrorException) ex;

    if (isMissingPathVariableError(e)) {
      String name = findParameterName(e.getMethodParameter());
      return Problem.builder()
          .status(HttpStatus.BAD_REQUEST.value())
          .detail(formatDetail(MISSING_PATH_VARIABLE_DETAIL))
          .extension(NAME_EXTENSION, name)
          .build();
    }

    return INTERNAL_SERVER_ERROR;
  }

  /**
   * Unlike other implementations of {@code AbstractNamedValueSyncArgumentResolver} that throw
   * {@code MissingRequestValueException} when certain {@code @RestController} argument is missing,
   * {@code PathVariableMethodArgumentResolver} throws {@code ServerErrorException}.
   *
   * <ul>
   *   <li>{@code MissingRequestValueException} (from {@code spring-web})
   *   <li>{@code ServerErrorException} (from {@code spring-web})
   *   <li>{@code AbstractNamedValueSyncArgumentResolver} (from {@code spring-webflux})
   *   <li>{@code PathVariableMethodArgumentResolver} (from {@code spring-webflux})
   * </ul>
   *
   * @see org.springframework.web.server.MissingRequestValueException
   * @see org.springframework.web.server.ServerErrorException
   * @return {@code true} if the exception actually refers to missing path variable, {@code false}
   *     otherwise
   * @since 1.2.0
   */
  private boolean isMissingPathVariableError(ServerErrorException e) {
    return e.getHandlerMethod() != null
        && AnnotationUtils.findAnnotation(e.getHandlerMethod(), RequestMapping.class) != null
        && e.getMethodParameter() != null
        && e.getMethodParameter().hasParameterAnnotation(PathVariable.class);
  }

  /**
   * Derives a path variable's logical name from the {@link MethodParameter}. If the parameter is
   * annotated with {@link PathVariable} and its {@code name} attribute is non-empty, that value is
   * returned; otherwise the Java parameter name (which may be {@code null} if not compiled with
   * {@code -parameters}) is returned.
   *
   * @param methodParameter the method parameter describing the missing path variable (never null
   *     when invoked)
   * @return explicit annotation name, falling back to the reflective parameter name (may be {@code
   *     null})
   * @since 1.2.0
   */
  private @Nullable String findParameterName(@Nullable MethodParameter methodParameter) {
    if (methodParameter == null) {
      return null;
    }
    PathVariable annotation = methodParameter.getParameterAnnotation(PathVariable.class);
    return (annotation != null && StringUtils.hasLength(annotation.name()))
        ? annotation.name()
        : methodParameter.getParameterName();
  }
}
