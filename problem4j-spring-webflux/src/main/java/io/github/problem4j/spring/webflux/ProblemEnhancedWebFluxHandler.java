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

package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.ResponseSupport.resolveStatus;
import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.logAdviceException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Handles Spring framework exceptions using registered {@code ProblemResolver}s.
 *
 * <p>This class extends {@link ResponseEntityExceptionHandler} and overrides {@link
 * #handleExceptionInternal} to replace the response body with a {@link Problem} object.
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>Delegates exception-to-problem mapping to {@link ProblemResolverStore}.
 *   <li>Sets content type to {@code application/problem+json}.
 *   <li>Falls back to {@link HttpStatus#INTERNAL_SERVER_ERROR} if mapping fails.
 * </ul>
 *
 * @see #handleExceptionInternal
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 * @since 1.2.0
 */
@RestControllerAdvice
public class ProblemEnhancedWebFluxHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ProblemEnhancedWebFluxHandler.class);

  private final ProblemResolverStore problemResolverStore;
  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebFluxInspector> adviceWebFluxInspectors;

  /**
   * Constructs a new {@code ProblemEnhancedWebFluxHandler}.
   *
   * @param problemResolverStore the resolver store for mapping exceptions
   * @param problemPostProcessor the post-processor for problems
   * @param adviceWebFluxInspectors the inspectors to apply to advice
   * @since 1.2.0
   */
  public ProblemEnhancedWebFluxHandler(
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebFluxInspector> adviceWebFluxInspectors) {
    this.problemResolverStore = problemResolverStore;
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebFluxInspectors = adviceWebFluxInspectors;
  }

  /**
   * Overrides the default exception handling to produce a {@link Problem} response. It attempts to
   * resolve the exception using registered resolvers, sets the content type, applies
   * post-processing, and falls back to a generic problem if resolution fails. It also applies any
   * configured inspectors.
   *
   * @param ex the exception to handle
   * @param body the body to use for the response
   * @param headers the headers to use for the response
   * @param status the status code to use for the response
   * @param exchange the current request and response
   * @return a {@link Mono} emitting the response entity with a {@link Problem} body
   * @since 1.2.0
   */
  @Override
  protected Mono<ResponseEntity<Object>> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      @Nullable HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    headers = headers != null ? new HttpHeaders(headers) : new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = getProblemForOverridingBody(context, ex, headers, status);
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    status = resolveStatus(problem);

    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return super.handleExceptionInternal(ex, problem, headers, status, exchange);
  }

  /**
   * Returns a {@link Problem} for the given exception, using a resolver if available, or a fallback
   * otherwise.
   *
   * @param context the problem context
   * @param ex the exception to resolve
   * @param headers the HTTP headers
   * @param status the HTTP status code
   * @return a {@link Problem} for the exception
   * @since 1.2.0
   */
  protected Problem getProblemForOverridingBody(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return problemResolverStore
        .findResolver(ex.getClass())
        .map(resolver -> resolver.resolve(context, ex, headers, status))
        .orElseGet(() -> fallbackProblem(status));
  }

  /**
   * Returns a fallback {@link Problem} with the given status.
   *
   * @param status the HTTP status code
   * @return a fallback {@link Problem}
   * @since 1.2.0
   */
  protected Problem fallbackProblem(HttpStatusCode status) {
    return Problem.of(status.value());
  }
}
