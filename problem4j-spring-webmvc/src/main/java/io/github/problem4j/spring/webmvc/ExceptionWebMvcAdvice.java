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
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Fallback exception handler for uncaught {@link Exception}s in Spring REST controllers.
 *
 * <p>This class uses {@link RestControllerAdvice} to intercept any exceptions not handled by more
 * specific exception handlers. It converts them into a standardized {@link Problem} response with:
 *
 * <ul>
 *   <li>HTTP status: {@link HttpStatus#INTERNAL_SERVER_ERROR}
 *   <li>Response body: a {@link Problem} object containing the status code and reason phrase
 *   <li>Content type: {@code application/problem+json}
 * </ul>
 *
 * <p>Intended as a <b>generic fallback</b>, it ensures that unexpected exceptions still produce a
 * consistent {@link Problem} response. For more specific exception handling, use {@link
 * ProblemResponseEntityExceptionHandler}, {@link ProblemExceptionWebMvcAdvice}.
 *
 * <p>Part of the advice path, registered with {@code problem4j.exception-handling.mode=advice}
 * (default). With {@code problem4j.exception-handling.mode=cooperate}, exceptions not handled by
 * the application are rendered by {@link ProblemHandlerExceptionResolver} instead.
 *
 * @since 1.2.0
 */
@Order(ExceptionWebMvcAdvice.PRECEDENCE)
@RestControllerAdvice
public class ExceptionWebMvcAdvice {

  /**
   * Precedence of this advice, {@code Ordered.LOWEST_PRECEDENCE}. It handles every exception, so it
   * is applied after every other controller advice. Inherited by subclasses, unless they declare
   * their own {@code @Order}.
   *
   * @since 3.1.0
   */
  public static final int PRECEDENCE = Ordered.LOWEST_PRECEDENCE;

  private static final Logger log = LoggerFactory.getLogger(ExceptionWebMvcAdvice.class);

  private final ProblemOperations problemOperations;
  private final List<ProblemInspector> problemInspectors;

  /**
   * Creates a new {@link ExceptionWebMvcAdvice}.
   *
   * @param problemMapper the problem mapper
   * @param problemResolverStore the resolver store
   * @param problemPostProcessor the post-processor
   * @param problemInspectors the inspectors to invoke after handling
   * @since 1.2.0
   * @deprecated use {@link #ExceptionWebMvcAdvice(ProblemOperations, List)} instead
   */
  @Deprecated(since = "3.1.0", forRemoval = true)
  public ExceptionWebMvcAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<? extends ProblemInspector> problemInspectors) {
    this(
        ProblemTemplate.builder()
            .problemMapper(problemMapper)
            .problemResolverStore(problemResolverStore)
            .problemPostProcessor(problemPostProcessor)
            .build(),
        problemInspectors);
  }

  /**
   * Creates a new {@link ExceptionWebMvcAdvice}, resolving and post-processing exceptions with
   * {@link ProblemOperations}.
   *
   * @param problemOperations the operations resolving and post-processing problems
   * @param problemInspectors the inspectors to invoke after handling
   * @since 3.1.0
   */
  public ExceptionWebMvcAdvice(
      ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
    this.problemOperations = problemOperations;
    this.problemInspectors = new ArrayList<>(problemInspectors);
  }

  /**
   * Generic fallback handler converting any uncaught exception into a {@link Problem} response.
   * Translated with {@link ProblemOperations#translate}, which uses the {@code Problem} of {@code
   * ProblemException}, {@code @ProblemMapping}, a resolver, {@code @ResponseStatus}, the status of
   * {@code ErrorResponse}, or defaults to {@code INTERNAL_SERVER_ERROR}.
   *
   * @param ex the uncaught exception
   * @param request the web request
   * @return a {@link ResponseEntity} containing the Problem response
   * @since 1.2.0
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleException(Exception ex, WebRequest request) {
    ProblemContext context =
        (ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE, SCOPE_REQUEST);
    if (context == null) {
      context = ProblemContext.create();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = problemOperations.translate(context, ex, headers);
    } catch (Exception e) {
      logAdviceException(log, ex, request, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (ProblemInspector inspector : problemInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    request.setAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE, problem, SCOPE_REQUEST);
    return new ResponseEntity<>(problem, headers, status);
  }
}
