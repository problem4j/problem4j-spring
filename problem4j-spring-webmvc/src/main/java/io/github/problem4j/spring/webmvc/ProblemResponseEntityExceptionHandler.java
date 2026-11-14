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

import static io.github.problem4j.spring.web.AttributeSupport.POST_PROCESSED_PROBLEM_ATTRIBUTE;
import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.logAdviceException;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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
 * <p>Part of the advice path, registered with {@code problem4j.exception-handling.mode=advice}
 * (default). With {@code problem4j.exception-handling.mode=cooperate}, Spring's exceptions are
 * rendered by {@link ProblemHandlerExceptionResolver} instead.
 *
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 * @since 3.1.0
 */
@Order(ProblemResponseEntityExceptionHandler.PRECEDENCE)
@RestControllerAdvice
public class ProblemResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  /**
   * Precedence of this advice, {@code Ordered.LOWEST_PRECEDENCE - 10}. It is applied before
   * controller advices of the application with default order, and before {@link
   * ExceptionWebMvcAdvice}. Inherited by subclasses, unless they declare their own {@code @Order}.
   *
   * @since 3.1.0
   */
  public static final int PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 10;

  private static final Logger log =
      LoggerFactory.getLogger(ProblemResponseEntityExceptionHandler.class);

  private final ProblemOperations problemOperations;
  private final List<ProblemInspector> problemInspectors;

  /**
   * Creates a new {@link ProblemResponseEntityExceptionHandler}.
   *
   * @param problemResolverStore the resolver store
   * @param problemPostProcessor the post-processor
   * @param problemInspectors the inspectors
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
   * Handles exceptions by resolving them to {@link Problem} objects, setting content type, and
   * applying inspectors.
   *
   * @param ex the exception to handle
   * @param body the body to use for the response
   * @param headers the headers to use for the response
   * @param status the status code to use for the response
   * @param request the current request
   * @return a {@link ResponseEntity} containing the resolved {@link Problem} and appropriate
   *     headers and status
   * @since 3.1.0
   */
  @Override
  protected @Nullable ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      @Nullable Object body,
      @Nullable HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemContext context =
        (ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE, SCOPE_REQUEST);
    if (context == null) {
      context = ProblemContext.create();
    }

    headers = headers != null ? new HttpHeaders(headers) : new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = getProblemForOverridingBody(context, ex, headers, status);
      problem = problemOperations.postProcess(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, request, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (ProblemInspector inspector : problemInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    request.setAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE, problem, SCOPE_REQUEST);
    return super.handleExceptionInternal(ex, problem, headers, status, request);
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
