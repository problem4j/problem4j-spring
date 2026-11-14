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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.util.DisconnectedClientHelper;

/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} rendering exceptions known to
 * Problem4J as {@code application/problem+json} responses, if they were not handled by any
 * application {@code @ExceptionHandler}.
 *
 * <p>Exceptions supported by {@link ProblemOperations#supports(Exception)} are resolved, i.e.
 * {@code ProblemException}s, {@code @ProblemMapping} annotated exceptions, exceptions with a
 * registered {@code ProblemResolver}, Spring's {@code ErrorResponse} exceptions and
 * {@code @ResponseStatus} annotated exceptions. With {@linkplain #setUnknownExceptions(boolean)
 * unknown exceptions} enabled, other exceptions are resolved as {@code 500 Internal Server Error}
 * problems and logged, otherwise they are left to the default error handling of Spring and Spring
 * Boot. Exceptions caused by disconnected clients are never resolved.
 *
 * <p>Registered by autoconfiguration right after Spring's {@code
 * ExceptionHandlerExceptionResolver}, so that {@code @ControllerAdvice}s and
 * {@code @RestControllerAdvice}s of the application always take precedence, regardless of their
 * order. Exceptions are not resolved if the response is already committed.
 *
 * <p>Part of the cooperate path, registered with {@code
 * problem4j.exception-handling.mode=cooperate}. With {@code
 * problem4j.exception-handling.mode=advice} (default), exceptions are handled by {@link
 * ExceptionWebMvcAdvice}, {@link ProblemExceptionWebMvcAdvice} and {@link
 * ProblemResponseEntityExceptionHandler} instead.
 *
 * @since 3.1.0
 */
public class ProblemHandlerExceptionResolver extends AbstractHandlerExceptionResolver {

  private static final Logger log = LoggerFactory.getLogger(ProblemHandlerExceptionResolver.class);

  private final ProblemOperations problemOperations;
  private final Supplier<? extends @Nullable List<HttpMessageConverter<?>>> messageConverters;
  private final List<ProblemInspector> inspectors;

  private boolean unknownExceptions = false;

  /**
   * Creates a new {@code ProblemHandlerExceptionResolver}.
   *
   * @param problemOperations the operations translating unhandled exceptions
   * @param messageConverters supplies converters used to write {@link Problem} responses
   * @param inspectors the inspectors invoked before writing {@link Problem} responses
   * @since 3.1.0
   */
  public ProblemHandlerExceptionResolver(
      ProblemOperations problemOperations,
      Supplier<? extends @Nullable List<HttpMessageConverter<?>>> messageConverters,
      List<? extends ProblemInspector> inspectors) {
    this.problemOperations = problemOperations;
    this.messageConverters = messageConverters;
    this.inspectors = new ArrayList<>(inspectors);
  }

  /**
   * Returns whether exceptions not known to Problem4J are resolved as {@code 500 Internal Server
   * Error} problems.
   *
   * @return {@code true} if unknown exceptions are rendered
   * @since 3.1.0
   */
  public boolean isUnknownExceptions() {
    return unknownExceptions;
  }

  /**
   * Sets whether exceptions not known to Problem4J are resolved as {@code 500 Internal Server
   * Error} problems, {@code false} by default.
   *
   * @param unknownExceptions whether unknown exceptions are rendered
   * @since 3.1.0
   */
  public void setUnknownExceptions(boolean unknownExceptions) {
    this.unknownExceptions = unknownExceptions;
  }

  /**
   * Translates the exception into a {@link Problem}, invokes inspectors and writes it to the
   * response.
   *
   * @param request the current request
   * @param response the current response
   * @param handler the executed handler, or {@code null} if none was chosen
   * @param ex the exception thrown during handler execution
   * @return an empty {@link ModelAndView} if the exception was resolved, {@code null} otherwise
   * @since 3.1.0
   */
  @Override
  protected @Nullable ModelAndView doResolveException(
      HttpServletRequest request,
      HttpServletResponse response,
      @Nullable Object handler,
      Exception ex) {
    if (response.isCommitted() || DisconnectedClientHelper.isClientDisconnectedException(ex)) {
      return null;
    }
    boolean known = problemOperations.supports(ex);
    if (!known && !unknownExceptions) {
      return null;
    }
    if (!known) {
      log.error(
          "Unhandled exception (method={}, endpoint={}, traceId={})",
          request.getMethod(),
          request.getRequestURI(),
          request.getAttribute(TRACE_ID_ATTRIBUTE),
          ex);
    }

    ProblemContext context =
        request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
            ? attribute
            : ProblemContext.create();
    HttpHeaders headers = new HttpHeaders();

    Problem problem;
    try {
      problem = problemOperations.translate(context, ex, headers);
    } catch (Exception e) {
      logTranslationFailure(request, ex, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    ServletWebRequest webRequest = new ServletWebRequest(request, response);
    for (ProblemInspector inspector : inspectors) {
      inspector.inspect(context, problem, ex, headers, status, webRequest);
    }

    try {
      writeProblem(response, problem, headers, status);
    } catch (IOException | RuntimeException e) {
      log.warn("Unable to write problem response for {}", ex.getClass().getName(), e);
      return null;
    }
    return new ModelAndView();
  }

  /**
   * Writes a {@link Problem} response as {@code application/problem+json}, keeping headers already
   * assigned to the response (e.g. tracing header).
   *
   * @param response the current response
   * @param problem the problem to write
   * @param headers additional response headers
   * @param status the response status
   * @throws IOException if writing the response fails
   * @since 3.1.0
   */
  @SuppressWarnings("unchecked")
  protected void writeProblem(
      HttpServletResponse response, Problem problem, HttpHeaders headers, HttpStatus status)
      throws IOException {
    response.resetBuffer();
    response.setStatus(status.value());
    headers.forEach((name, values) -> {
      for (int i = 0; i < values.size(); i++) {
        if (i == 0) {
          response.setHeader(name, values.get(i));
        } else {
          response.addHeader(name, values.get(i));
        }
      }
    });
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

    List<HttpMessageConverter<?>> converters = messageConverters.get();
    if (converters != null) {
      for (HttpMessageConverter<?> converter : converters) {
        if (converter.canWrite(problem.getClass(), MediaType.APPLICATION_PROBLEM_JSON)) {
          ServletServerHttpResponse outputMessage = new ServletServerHttpResponse(response);
          ((HttpMessageConverter<Object>) converter)
              .write(problem, MediaType.APPLICATION_PROBLEM_JSON, outputMessage);
          outputMessage.flush();
          return;
        }
      }
    }
    throw new IllegalStateException("No HttpMessageConverter for " + problem.getClass());
  }

  private void logTranslationFailure(HttpServletRequest request, Exception ex, Exception e) {
    log.warn(
        "Unable to resolve problem response (method={}, endpoint={}, traceId={}, message={}, originalException=[{} : {}])",
        request.getMethod(),
        request.getRequestURI(),
        request.getAttribute(TRACE_ID_ATTRIBUTE),
        e.getMessage(),
        ex.getClass().getName(),
        ex.getMessage(),
        e);
  }
}
