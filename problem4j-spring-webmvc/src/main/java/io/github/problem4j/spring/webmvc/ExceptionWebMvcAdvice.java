/*
 * Copyright 2025-2026 The Problem4J Authors
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
import io.github.problem4j.core.ProblemBuilder;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemMapper;
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
 *
 * @since 1.2.0
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
   * @since 1.2.0
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
      problem = getProblem(ex, context, headers);
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, request, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus status =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

    for (AdviceWebMvcInspector inspector : adviceWebMvcInspectors) {
      inspector.inspect(context, problem, ex, headers, status, request);
    }

    return new ResponseEntity<>(problem, headers, status);
  }

  private Problem getProblem(Exception ex, ProblemContext context, HttpHeaders headers) {
    if (problemMapper.isMappingCandidate(ex)) {
      return problemMapper.toProblemBuilder(ex, context).build();
    }
    Optional<ProblemResolver> optionalResolver = problemResolverStore.findResolver(ex.getClass());
    if (optionalResolver.isPresent()) {
      return optionalResolver.get().resolve(context, ex, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    ResponseStatus responseStatus =
        AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
    if (responseStatus != null) {
      ProblemBuilder builder = Problem.builder().status(responseStatus.code().value());
      if (StringUtils.hasLength(responseStatus.reason())) {
        builder = builder.detail(responseStatus.reason());
      }
      return builder.build();
    }
    return Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}
