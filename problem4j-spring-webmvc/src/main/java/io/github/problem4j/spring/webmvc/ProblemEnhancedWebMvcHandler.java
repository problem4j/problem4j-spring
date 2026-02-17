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
import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;
import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.logAdviceException;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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
 *   <li>Delegates exception-to-problem mapping to {@link ProblemResolverStore}.
 *   <li>Sets content type to {@code application/problem+json}.
 *   <li>Falls back to {@link ProblemStatus#INTERNAL_SERVER_ERROR} if mapping fails.
 * </ul>
 *
 * @see io.github.problem4j.spring.web.resolver.ProblemResolver
 */
@RestControllerAdvice
public class ProblemEnhancedWebMvcHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ProblemEnhancedWebMvcHandler.class);

  private final ProblemResolverStore problemResolverStore;
  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebMvcInspector> adviceWebMvcInspectors;

  /**
   * Creates a new {@link ProblemEnhancedWebMvcHandler}.
   *
   * @param problemResolverStore the resolver store
   * @param problemPostProcessor the post-processor
   * @param adviceWebMvcInspectors the inspectors
   */
  public ProblemEnhancedWebMvcHandler(
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
    this.problemResolverStore = problemResolverStore;
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebMvcInspectors = adviceWebMvcInspectors;
  }

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
      problem = getBuilderForOverridingBody(context, ex, headers, status).build();
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, request, e);
      problem = Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build();
    }

    status = resolveStatus(problem);

    for (AdviceWebMvcInspector inspector : adviceWebMvcInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    return super.handleExceptionInternal(ex, problem, headers, status, request);
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
