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
package io.github.problem4j.spring.webflux;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;
import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.logAdviceException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
 *   <li>Falls back to {@link ProblemStatus#INTERNAL_SERVER_ERROR} if mapping fails.
 * </ul>
 *
 * @see #handleExceptionInternal
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
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
   * <b>Note:</b> Although {@link HttpHeaders#writableHttpHeaders(HttpHeaders)} is deprecated, it is
   * used here for backward compatibility with older Spring Framework versions.
   *
   * <p>The deprecation alternative provided by Spring is not available in versions {@code 6.0.*}
   * and {@code 6.1.*} (Spring Framework versions, not Spring Boot). Therefore, this method is
   * retained to ensure compatibility across those versions.
   */
  @Override
  @SuppressWarnings("removal")
  protected Mono<ResponseEntity<Object>> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    headers = headers != null ? HttpHeaders.writableHttpHeaders(headers) : new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = getBuilderForOverridingBody(context, ex, headers, status).build();
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build();
    }

    status = resolveStatus(problem);

    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return super.handleExceptionInternal(ex, problem, headers, status, exchange);
  }

  /**
   * Returns a {@link ProblemBuilder} for the given exception, using a resolver if available, or a
   * fallback otherwise.
   *
   * @param context the problem context
   * @param ex the exception to resolve
   * @param headers the HTTP headers
   * @param status the HTTP status code
   * @return a {@link ProblemBuilder} for the exception
   */
  protected ProblemBuilder getBuilderForOverridingBody(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    return problemResolverStore
        .findResolver(ex.getClass())
        .map(resolver -> resolver.resolveBuilder(context, ex, headers, status))
        .orElseGet(() -> fallbackProblem(status));
  }

  /**
   * Returns a fallback {@link ProblemBuilder} with the given status.
   *
   * @param status the HTTP status code
   * @return a fallback {@link ProblemBuilder}
   */
  protected ProblemBuilder fallbackProblem(HttpStatusCode status) {
    return Problem.builder().status(resolveStatus(status));
  }
}
