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
import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.logAdviceException;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.ProblemPostProcessor;
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
 * Handles {@link ProblemException} thrown by application code.
 *
 * <p>Converts the exception into a {@link Problem} response with the appropriate HTTP status and
 * content type {@code application/problem+json}.
 *
 * <p>This is intended for application-level exceptions already represented as {@link Problem}.
 *
 * @since 1.2.0
 */
@Order(ProblemExceptionWebMvcAdvice.PRECEDENCE)
@RestControllerAdvice
public class ProblemExceptionWebMvcAdvice {

  /**
   * Precedence of this advice, {@code Ordered.LOWEST_PRECEDENCE - 10}. It is applied before
   * controller advices of the application with default order, and before {@link
   * ExceptionWebMvcAdvice}. Inherited by subclasses, unless they declare their own {@code @Order}.
   *
   * @since 3.1.0
   */
  public static final int PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 10;

  private static final Logger log = LoggerFactory.getLogger(ProblemExceptionWebMvcAdvice.class);

  private final ProblemPostProcessor problemPostProcessor;

  private final List<ProblemInspector> problemInspectors;

  /**
   * Creates a new {@code ProblemExceptionWebMvcAdvice}.
   *
   * @param problemPostProcessor the post-processor
   * @param problemInspectors the list of inspectors
   * @since 1.2.0
   */
  public ProblemExceptionWebMvcAdvice(
      ProblemPostProcessor problemPostProcessor,
      List<? extends ProblemInspector> problemInspectors) {
    this.problemPostProcessor = problemPostProcessor;
    this.problemInspectors = new ArrayList<>(problemInspectors);
  }

  /**
   * Converts a {@link ProblemException} into a {@code Problem} response. The contained {@link
   * Problem} is post-processed, headers set to application/problem+json, and status resolved from
   * the problem's status code.
   *
   * @param ex the ProblemException to handle
   * @param request the web request
   * @return a {@link ResponseEntity} containing the {@link Problem}
   * @since 1.2.0
   */
  @ExceptionHandler(ProblemException.class)
  public ResponseEntity<Problem> handleProblemException(ProblemException ex, WebRequest request) {
    ProblemContext context =
        (ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE, SCOPE_REQUEST);
    if (context == null) {
      context = ProblemContext.create();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    Problem problem;
    try {
      problem = ex.getProblem();
      problem = problemPostProcessor.process(context, problem);
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

    return new ResponseEntity<>(problem, headers, status);
  }
}
