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
package io.github.problem4j.spring.web.resolver;

import static io.github.problem4j.spring.web.ProblemSupport.MISSING_PATH_VARIABLE_DETAIL;
import static io.github.problem4j.spring.web.ProblemSupport.NAME_EXTENSION;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.IdentityProblemFormat;
import io.github.problem4j.spring.web.ProblemFormat;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
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
 */
public class ServerErrorProblemResolver extends AbstractProblemResolver {

  /** Creates a new {@link ServerErrorProblemResolver} with default problem format. */
  public ServerErrorProblemResolver() {
    this(new IdentityProblemFormat());
  }

  /**
   * Creates a new {@link ServerErrorProblemResolver} with the specified problem format.
   *
   * @param problemFormat the problem format to use
   */
  public ServerErrorProblemResolver(ProblemFormat problemFormat) {
    super(ServerErrorException.class, problemFormat);
  }

  /**
   * Resolves a {@link ServerErrorException} into a {@link ProblemBuilder}.
   *
   * <p>Special case: Spring WebFlux's {@code PathVariableMethodArgumentResolver} raises {@link
   * ServerErrorException} (instead of a missing-value exception) when a required {@code
   * PathVariable} is absent. In that scenario this method returns a BAD_REQUEST problem with a
   * standardized detail ({@code ProblemSupport#MISSING_PATH_VARIABLE_DETAIL}) and an extension
   * "{@code ProblemSupport#NAME_EXTENSION}" containing the variable name.
   *
   * <p>Otherwise, it falls back to a generic INTERNAL_SERVER_ERROR problem.
   *
   * @param context problem context (unused)
   * @param ex the triggering {@link ServerErrorException}
   * @param headers HTTP headers (unused)
   * @param status suggested status from caller (ignored; derives from condition)
   * @return builder with BAD_REQUEST + path variable info or INTERNAL_SERVER_ERROR
   * @see io.github.problem4j.spring.web.ProblemSupport#MISSING_PATH_VARIABLE_DETAIL
   * @see io.github.problem4j.spring.web.ProblemSupport#NAME_EXTENSION
   * @see org.springframework.web.bind.annotation.PathVariable
   */
  @Override
  public ProblemBuilder resolveBuilder(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    ServerErrorException e = (ServerErrorException) ex;

    if (isMissingPathVariableError(e)) {
      String name = findParameterName(e.getMethodParameter());
      return Problem.builder()
          .status(ProblemStatus.BAD_REQUEST)
          .detail(formatDetail(MISSING_PATH_VARIABLE_DETAIL))
          .extension(NAME_EXTENSION, name);
    }

    return Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR);
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
   */
  private String findParameterName(MethodParameter methodParameter) {
    if (methodParameter == null) {
      return null;
    }
    PathVariable annotation = methodParameter.getParameterAnnotation(PathVariable.class);
    return (annotation != null && StringUtils.hasLength(annotation.name()))
        ? annotation.name()
        : methodParameter.getParameterName();
  }
}
