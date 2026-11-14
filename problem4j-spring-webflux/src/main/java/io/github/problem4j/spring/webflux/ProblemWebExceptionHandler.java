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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Hints;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.util.DisconnectedClientHelper;
import reactor.core.publisher.Mono;

/**
 * {@link WebExceptionHandler} rendering exceptions known to Problem4J as {@code
 * application/problem+json} responses, if they were not handled by any application exception
 * handler.
 *
 * <p>Exceptions supported by {@link ProblemOperations#supports(Exception)} are handled, i.e. {@code
 * ProblemException}s, {@code @ProblemMapping} annotated exceptions, exceptions with a registered
 * {@code ProblemResolver}, Spring's {@code ErrorResponse} exceptions and {@code @ResponseStatus}
 * annotated exceptions. With {@linkplain #setUnknownExceptions(boolean) unknown exceptions}
 * enabled, other exceptions are handled as {@code 500 Internal Server Error} problems and logged,
 * otherwise they are passed on to the next web exception handler, typically the {@code
 * ErrorWebExceptionHandler} of Spring Boot. Errors caused by disconnected clients are never
 * handled.
 *
 * <p>Exceptions handled by application {@code @ControllerAdvice}s and
 * {@code @RestControllerAdvice}s never reach web exception handlers, so they always take
 * precedence, regardless of their order. Exceptions of {@code WebFilter}s are handled too. Errors
 * are propagated as they are if the response is already committed.
 *
 * <p>Part of the deferred path, registered with {@code problem4j.exception-handling.mode=deferred}.
 * With {@code problem4j.exception-handling.mode=advice} (default), exceptions are handled by {@link
 * ExceptionWebFluxAdvice}, {@link ProblemExceptionWebFluxAdvice} and {@link
 * ProblemResponseEntityExceptionHandler} instead.
 *
 * @since 3.1.0
 */
public class ProblemWebExceptionHandler implements WebExceptionHandler, Ordered {

  private static final Logger log = LoggerFactory.getLogger(ProblemWebExceptionHandler.class);

  private final ProblemOperations problemOperations;
  private final List<HttpMessageWriter<?>> messageWriters;
  private final List<ProblemInspector> inspectors;

  private int order = -3;

  private boolean unknownExceptions = false;

  /**
   * Creates a new {@code ProblemWebExceptionHandler}.
   *
   * @param problemOperations the operations translating unhandled exceptions
   * @param messageWriters the writers used to write {@link Problem} responses
   * @param inspectors the inspectors invoked before writing {@link Problem} responses
   * @since 3.1.0
   */
  public ProblemWebExceptionHandler(
      ProblemOperations problemOperations,
      List<HttpMessageWriter<?>> messageWriters,
      List<? extends ProblemInspector> inspectors) {
    this.problemOperations = problemOperations;
    this.messageWriters = new ArrayList<>(messageWriters);
    this.inspectors = new ArrayList<>(inspectors);
  }

  /**
   * Returns the order of this handler, {@code -3} by default, which precedes {@code
   * ErrorWebExceptionHandler} of Spring Boot ({@code -1}).
   *
   * @return the order of this handler
   * @since 3.1.0
   */
  @Override
  public int getOrder() {
    return order;
  }

  /**
   * Sets the order of this handler.
   *
   * @param order the order of this handler
   * @since 3.1.0
   */
  public void setOrder(int order) {
    this.order = order;
  }

  /**
   * Returns whether exceptions not known to Problem4J are handled as {@code 500 Internal Server
   * Error} problems.
   *
   * @return {@code true} if unknown exceptions are rendered
   * @since 3.1.0
   */
  public boolean isUnknownExceptions() {
    return unknownExceptions;
  }

  /**
   * Sets whether exceptions not known to Problem4J are handled as {@code 500 Internal Server Error}
   * problems, {@code false} by default.
   *
   * @param unknownExceptions whether unknown exceptions are rendered
   * @since 3.1.0
   */
  public void setUnknownExceptions(boolean unknownExceptions) {
    this.unknownExceptions = unknownExceptions;
  }

  /**
   * Translates an unhandled exception into a {@link Problem}, invokes inspectors and writes it to
   * the response.
   *
   * @param exchange the current server exchange
   * @param ex the unhandled error
   * @return a {@link Mono} that completes when the response is written
   * @since 3.1.0
   */
  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (exchange.getResponse().isCommitted()
        || !(ex instanceof Exception exception)
        || DisconnectedClientHelper.isClientDisconnectedException(ex)) {
      return Mono.error(ex);
    }
    boolean known = problemOperations.supports(exception);
    if (!known && !unknownExceptions) {
      return Mono.error(ex);
    }
    if (!known) {
      log.error(
          "Unhandled exception (method={}, path={}, traceId={})",
          exchange.getRequest().getMethod(),
          exchange.getRequest().getPath(),
          exchange.getAttribute(TRACE_ID_ATTRIBUTE),
          ex);
    }

    ProblemContext context =
        exchange.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
            ? attribute
            : ProblemContext.create();
    HttpHeaders headers = new HttpHeaders();

    Problem problem;
    try {
      problem = problemOperations.translate(context, exception, headers);
    } catch (Exception e) {
      logTranslationFailure(exchange, exception, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (ProblemInspector inspector : inspectors) {
      inspector.inspect(context, problem, exception, headers, status, exchange);
    }

    return writeProblem(exchange, problem, headers, status, ex);
  }

  /**
   * Writes a {@link Problem} response as {@code application/problem+json}, keeping headers already
   * assigned to the response (e.g. tracing header).
   *
   * @param exchange the current server exchange
   * @param problem the problem to write
   * @param headers additional response headers
   * @param status the response status
   * @param ex the unhandled error, propagated if no writer supports the problem
   * @return a {@link Mono} that completes when the response is written
   * @since 3.1.0
   */
  @SuppressWarnings("unchecked")
  protected Mono<Void> writeProblem(
      ServerWebExchange exchange,
      Problem problem,
      HttpHeaders headers,
      HttpStatus status,
      Throwable ex) {
    ResolvableType type = ResolvableType.forInstance(problem);
    for (HttpMessageWriter<?> writer : messageWriters) {
      if (writer.canWrite(type, MediaType.APPLICATION_PROBLEM_JSON)) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        HttpHeaders responseHeaders = response.getHeaders();
        responseHeaders.remove(HttpHeaders.CONTENT_LENGTH);
        headers.forEach(responseHeaders::put);
        responseHeaders.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return ((HttpMessageWriter<Object>) writer)
            .write(
                Mono.just(problem),
                type,
                MediaType.APPLICATION_PROBLEM_JSON,
                response,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.getLogPrefix()));
      }
    }
    return Mono.error(ex);
  }

  private void logTranslationFailure(ServerWebExchange exchange, Exception ex, Exception e) {
    log.warn(
        "Unable to resolve problem response (method={}, path={}, traceId={}, message={}, originalException=[{} : {}])",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath(),
        exchange.getAttribute(TRACE_ID_ATTRIBUTE),
        e.getMessage(),
        ex.getClass().getName(),
        ex.getMessage(),
        e);
  }
}
