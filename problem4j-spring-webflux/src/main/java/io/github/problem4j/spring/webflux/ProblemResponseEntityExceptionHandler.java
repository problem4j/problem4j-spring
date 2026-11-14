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
import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.logAdviceException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
 *   <li>Delegates exception-to-problem mapping to {@code ProblemOperations}, with the status chosen
 *       by Spring.
 *   <li>Sets content type to {@code application/problem+json}.
 *   <li>Falls back to {@link HttpStatus#INTERNAL_SERVER_ERROR} if mapping fails.
 * </ul>
 *
 * @see #handleExceptionInternal
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 * @since 3.1.0
 */
@RestControllerAdvice
public class ProblemResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log =
      LoggerFactory.getLogger(ProblemResponseEntityExceptionHandler.class);

  private final ProblemOperations problemOperations;
  private final List<ProblemInspector> problemInspectors;

  /**
   * Constructs a new {@code ProblemResponseEntityExceptionHandler}.
   *
   * @param problemResolverStore the resolver store for mapping exceptions
   * @param problemPostProcessor the post-processor for problems
   * @param problemInspectors the inspectors to apply to advice
   * @since 3.1.0
   * @deprecated use {@link #ProblemResponseEntityExceptionHandler(ProblemOperations, List)} instead
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ProblemResponseEntityExceptionHandler(
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<? extends ProblemInspector> problemInspectors) {
    this(
        ProblemTemplate.builder()
            .problemResolverStore(problemResolverStore)
            .problemPostProcessor(problemPostProcessor)
            .build(),
        problemInspectors);
  }

  /**
   * Creates a new {@link ProblemResponseEntityExceptionHandler}, resolving and post-processing
   * exceptions with {@link ProblemOperations}, with the status chosen by Spring.
   *
   * @param problemOperations the operations resolving and post-processing problems
   * @param problemInspectors the inspectors
   * @since 3.1.0
   */
  public ProblemResponseEntityExceptionHandler(
      ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
    this.problemOperations = problemOperations;
    this.problemInspectors = new ArrayList<>(problemInspectors);
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
   * @since 3.1.0
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
      problem = problemOperations.postProcess(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (ProblemInspector inspector : problemInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return super.handleExceptionInternal(ex, problem, headers, status, exchange);
  }

  /**
   * Returns a {@link Problem} for the given exception, resolved with {@link ProblemOperations} if
   * it supports the exception, or {@link #fallbackProblem} otherwise.
   *
   * @param context the problem context
   * @param ex the exception to resolve
   * @param headers the HTTP headers
   * @param status the HTTP status code
   * @return a {@link Problem} for the exception
   * @since 3.1.0
   */
  protected Problem getProblemForOverridingBody(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return problemOperations.supports(ex)
        ? problemOperations.resolve(context, ex, headers, status)
        : fallbackProblem(status);
  }

  /**
   * Returns a fallback {@link Problem} with the given status, for exceptions not supported by
   * {@link ProblemOperations}.
   *
   * @param status the HTTP status code
   * @return a fallback {@link Problem}
   * @since 3.1.0
   */
  protected Problem fallbackProblem(HttpStatusCode status) {
    return Problem.of(status.value());
  }
}
