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
package io.github.problem4j.spring.webmvc;

import static io.github.problem4j.spring.web.AttributeSupport.PROBLEM_CONTEXT_ATTRIBUTE;
import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;
import static io.github.problem4j.spring.webmvc.WebMvcAdviceSupport.logAdviceException;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
import io.github.problem4j.core.ProblemStatus;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.ProblemSupport;
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
 * ProblemEnhancedWebMvcHandler}, {@link ProblemExceptionWebMvcAdvice}.
 */
@RestControllerAdvice
public class ExceptionWebMvcAdvice {

  private static final Logger log = LoggerFactory.getLogger(ExceptionWebMvcAdvice.class);

  private final ProblemMapper problemMapper;
  private final ProblemResolverStore problemResolverStore;
  private final ProblemPostProcessor problemPostProcessor;

  private final List<AdviceWebMvcInspector> adviceWebMvcInspectors;

  /**
   * Creates a new {@link ExceptionWebMvcAdvice}.
   *
   * @param problemMapper the problem mapper
   * @param problemResolverStore the resolver store
   * @param problemPostProcessor the post-processor
   * @param adviceWebMvcInspectors the inspectors to invoke after handling
   */
  public ExceptionWebMvcAdvice(
      ProblemMapper problemMapper,
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<AdviceWebMvcInspector> adviceWebMvcInspectors) {
    this.problemMapper = problemMapper;
    this.problemResolverStore = problemResolverStore;
    this.problemPostProcessor = problemPostProcessor;
    this.adviceWebMvcInspectors = adviceWebMvcInspectors;
  }

  /**
   * Generic fallback handler converting any uncaught exception into a {@link Problem} response.
   * Chooses a resolver, {@code @ProblemMapping}, {@code @ResponseStatus}, or defaults to {@code
   * INTERNAL_SERVER_ERROR}.
   *
   * @param ex the uncaught exception
   * @param request the web request
   * @return a {@link ResponseEntity} containing the Problem response
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
      problem = getProblemBuilder(ex, context, headers).build();
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, request, e);
      problem = Problem.builder().status(ProblemStatus.INTERNAL_SERVER_ERROR).build();
    }

    HttpStatus status = ProblemSupport.resolveStatus(problem);

    for (AdviceWebMvcInspector inspector : adviceWebMvcInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    return new ResponseEntity<>(problem, headers, status);
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
