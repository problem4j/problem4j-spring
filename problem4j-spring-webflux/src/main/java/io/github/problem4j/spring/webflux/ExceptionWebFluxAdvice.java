/*
 * Copyright (c) 2025-2026 The Problem4J Authors
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
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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
 */
@RestControllerAdvice
public class ExceptionWebFluxAdvice {

  private static final Logger log = LoggerFactory.getLogger(ExceptionWebFluxAdvice.class);

  private final ProblemMapper problemMapper;
  private final ProblemResolverStore problemResolverStore;
  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebFluxInspector> adviceWebFluxInspectors;

  /**
   * Constructs a new {@code ExceptionWebFluxAdvice}.
   *
   * @param problemMapper the problem mapper to use
   * @param problemResolverStore the resolver store for mapping exceptions
   * @param problemPostProcessor the post-processor for problems
   * @param adviceWebFluxInspectors the inspectors to apply to advice
   */
  public ExceptionWebFluxAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebFluxInspector> adviceWebFluxInspectors) {
    this.problemMapper = problemMapper;
    this.problemResolverStore = problemResolverStore;
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebFluxInspectors = adviceWebFluxInspectors;
  }

  /**
   * Generic fallback handler converting any uncaught exception into a {@link Problem} response.
   * Chooses a resolver, {@code @ProblemMapping}, {@code @ResponseStatus}, or defaults to {@code
   * INTERNAL_SERVER_ERROR}.
   *
   * @param ex the exception to handle
   * @param exchange the current server web exchange
   * @return a {@link Mono} emitting the response entity with a {@link Problem} body
   */
  @ExceptionHandler(Exception.class)
  public Mono<ResponseEntity<Problem>> handleException(Exception ex, ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = getProblemBuilder(ex, context, headers).build();
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status = resolveStatus(problem);

    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return Mono.just(new ResponseEntity<>(problem, headers, status));
  }

  private ProblemBuilder getProblemBuilder(
      Exception ex, ProblemContext context, HttpHeaders headers) {
    ProblemBuilder builder;
    if (problemMapper.isMappingCandidate(ex)) {
      builder = problemMapper.toProblemBuilder(ex, context);
    } else {
      Optional<ProblemResolver> optionalResolver = problemResolverStore.findResolver(ex.getClass());
      if (optionalResolver.isPresent()) {
        builder =
            optionalResolver
                .get()
                .resolveBuilder(context, ex, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      } else {
        ResponseStatus responseStatus =
            AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
          builder = Problem.builder().status(resolveStatus(responseStatus.code()));
          if (StringUtils.hasLength(responseStatus.reason())) {
            builder = builder.detail(responseStatus.reason());
          }
        } else {
          builder = Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR);
        }
      }
    }
    return builder;
  }
}
