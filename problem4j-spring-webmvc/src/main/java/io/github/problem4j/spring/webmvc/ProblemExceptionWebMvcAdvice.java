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

package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.logAdviceException;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
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
import org.springframework.web.context.request.WebRequest;

/**
 * Handles {@link ProblemException} thrown by application code.
 *
 * <p>Converts the exception into a {@link Problem} response with the appropriate HTTP status and
 * content type {@code application/problem+json}.
 *
 * <p>This is intended for application-level exceptions already represented as {@link Problem}.
 */
@RestControllerAdvice
public class ProblemExceptionWebMvcAdvice {

  private static final Logger log = LoggerFactory.getLogger(ProblemExceptionWebMvcAdvice.class);

  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebMvcInspector> adviceWebMvcInspectors;

  /**
   * Creates a new {@code ProblemExceptionWebMvcAdvice}.
   *
   * @param problemPostProcessor the post-processor
   * @param adviceWebMvcInspectors the list of inspectors
   */
  public ProblemExceptionWebMvcAdvice(
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebMvcInspectors = adviceWebMvcInspectors;
  }

  /**
   * Converts a {@link ProblemException} into a {@code Problem} response. The contained {@link
   * Problem} is post-processed, headers set to application/problem+json, and status resolved from
   * the problem's status code.
   *
   * @param ex the ProblemException to handle
   * @param request the web request
   * @return a {@link ResponseEntity} containing the {@link Problem}
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

    HttpStatus status = ProblemSupport.resolveStatus(problem);

    for (AdviceWebMvcInspector inspector : adviceWebMvcInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    return new ResponseEntity<>(problem, headers, status);
  }
}
