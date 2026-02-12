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
import static io.github.problem4j.spring.webflux.WebFluxAdviceSupport.logAdviceException;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemSupport;
import java.util.List;
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
 * Handles {@link ProblemException} thrown by application code.
 *
 * <p>Converts the exception into a {@link Problem} response with the appropriate HTTP status and
 * content type {@code application/problem+json}.
 *
 * <p>This is intended for application-level exceptions already represented as {@link Problem}.
 */
@RestControllerAdvice
public class ProblemExceptionWebFluxAdvice {

  private static final Logger log = LoggerFactory.getLogger(ProblemExceptionWebFluxAdvice.class);

  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebFluxInspector> adviceWebFluxInspectors;

  /**
   * Constructs a new {@code ProblemExceptionWebFluxAdvice}.
   *
   * @param problemPostProcessor the post-processor for problems
   * @param adviceWebFluxInspectors the inspectors to apply to advice
   */
  public ProblemExceptionWebFluxAdvice(
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebFluxInspector> adviceWebFluxInspectors) {
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebFluxInspectors = adviceWebFluxInspectors;
  }

  /**
   * Converts a {@link ProblemException} into a {@code Problem} response: processes the embedded
   * {@link Problem}, sets content type, resolves status, and applies inspectors.
   *
   * @param ex the ProblemException to handle
   * @param exchange the current server web exchange
   * @return a {@link Mono} emitting the response entity with a {@link Problem} body
   */
  @ExceptionHandler(ProblemException.class)
  public Mono<ResponseEntity<Problem>> handleProblemException(
      ProblemException ex, ServerWebExchange exchange) {
    ProblemContext context =
        exchange.getAttributeOrDefault(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = ex.getProblem();
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, exchange, e);
      problem = Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build();
    }

    HttpStatus status = ProblemSupport.resolveStatus(problem);

    for (AdviceWebFluxInspector inspector : adviceWebFluxInspectors) {
      inspector.inspect(context, problem, ex, headers, status, exchange);
    }

    return Mono.just(new ResponseEntity<>(problem, headers, status));
  }
}
