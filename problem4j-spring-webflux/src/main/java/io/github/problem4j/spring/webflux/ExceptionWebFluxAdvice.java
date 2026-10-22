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
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.resolver.DefaultProblemResolutionFacade;
import io.github.problem4j.spring.web.resolver.ProblemResolutionFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
 * ProblemEnhancedWebFluxHandler}, {@link ProblemExceptionWebFluxAdvice}.
 *
 * @since 1.2.0
 * @deprecated since 3.1.0, as it is no longer registered by autoconfiguration. Exceptions not
 *     handled by any other exception handler are resolved by {@link
 *     ProblemErrorWebExceptionHandler}, using {@link ProblemResolutionFacade}.
 */
@Deprecated(since = "3.1.0", forRemoval = true)
@RestControllerAdvice
public class ExceptionWebFluxAdvice {

  private static final Logger log = LoggerFactory.getLogger(ExceptionWebFluxAdvice.class);

  private final ProblemResolutionFacade problemResolutionFacade;
  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebFluxInspector> adviceWebFluxInspectors;

  /**
   * Constructs a new {@code ExceptionWebFluxAdvice}.
   *
   * @param problemMapper the problem mapper to use
   * @param problemResolverStore the resolver store for mapping exceptions
   * @param problemPostProcessor the post-processor for problems
   * @param adviceWebFluxInspectors the inspectors to apply to advice
   * @since 1.2.0
   */
  public ExceptionWebFluxAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<? extends AdviceWebFluxInspector> adviceWebFluxInspectors) {
    this.problemResolutionFacade =
        new DefaultProblemResolutionFacade(problemMapper, problemResolverStore);
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebFluxInspectors = new ArrayList<>(adviceWebFluxInspectors);
  }

  /**
   * Generic fallback handler converting any uncaught exception into a {@link Problem} response.
   * Chooses a resolver, {@code @ProblemMapping}, {@code @ResponseStatus}, or defaults to {@code
   * INTERNAL_SERVER_ERROR}.
   *
   * @param ex the exception to handle
   * @param exchange the current server web exchange
   * @return a {@link Mono} emitting the response entity with a {@link Problem} body
   * @since 1.2.0
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Problem>> handleException(Exception ex, ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem =
          problemResolutionFacade.resolve(context, ex, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return Mono.just(new ResponseEntity<>(problem, headers, status));
  }
}
