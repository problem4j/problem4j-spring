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
import static io.github.problem4j.spring.web.AttributeSupport.TRACE_ID_ATTRIBUTE;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.autoconfigure.error.AbstractErrorController;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * A simple {@link org.springframework.boot.webmvc.error.ErrorController} implementation that
 * returns HTTP problems (RFC 7807) instead of HTML error pages.
 *
 * <p>It converts generic servlet errors into {@link Problem} responses with the appropriate HTTP
 * status and content type {@code application/problem+json}. It handles errors that do not pass
 * through Spring MVC exception resolvers, such as {@code response.sendError(...)} or exceptions
 * thrown by servlet filters.
 *
 * <p>If created with {@link ProblemOperations} (with {@code
 * problem4j.exception-handling.mode=cooperate}), the exception that caused the error is translated
 * the same way as by {@link ProblemHandlerExceptionResolver}, and {@link ProblemInspector}s are
 * invoked. Otherwise, only the status of the error is used.
 *
 * <p>Part of both paths, registered regardless of {@code problem4j.exception-handling.mode} (unless
 * {@code problem4j.webmvc.error-controller.enabled=false}). With {@code
 * problem4j.exception-handling.mode=advice} (default), only the status of the error is used; with
 * {@code problem4j.exception-handling.mode=cooperate}, the exception that caused the error is
 * translated the same way as by {@link ProblemHandlerExceptionResolver}.
 *
 * @since 1.2.0
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ProblemErrorController extends AbstractErrorController {

  private static final Logger log = LoggerFactory.getLogger(ProblemErrorController.class);

  private final ProblemPostProcessor problemPostProcessor;
  private final @Nullable ProblemOperations problemOperations;
  private final ErrorAttributes errorAttributes;
  private final List<ProblemInspector> problemInspectors;

  /**
   * Creates a new {@link ProblemErrorController}, which uses only the status of the error.
   *
   * @param problemPostProcessor the post-processor to use
   * @param errorAttributes the error attributes used to obtain error information
   * @since 1.2.0
   */
  public ProblemErrorController(
      ProblemPostProcessor problemPostProcessor, ErrorAttributes errorAttributes) {
    super(errorAttributes);
    this.problemPostProcessor = problemPostProcessor;
    this.problemOperations = null;
    this.errorAttributes = errorAttributes;
    this.problemInspectors = List.of();
  }

  /**
   * Creates a new {@link ProblemErrorController}, which translates the exception that caused the
   * error with {@link ProblemOperations}.
   *
   * @param problemOperations the operations translating exceptions and post-processing problems
   * @param errorAttributes the error attributes used to obtain error information
   * @param problemInspectors the inspectors invoked for translated exceptions
   * @since 3.1.0
   */
  public ProblemErrorController(
      ProblemOperations problemOperations,
      ErrorAttributes errorAttributes,
      List<? extends ProblemInspector> problemInspectors) {
    super(errorAttributes);
    this.problemPostProcessor = problemOperations::postProcess;
    this.problemOperations = problemOperations;
    this.errorAttributes = errorAttributes;
    this.problemInspectors = new ArrayList<>(problemInspectors);
  }

  /**
   * Handles all requests to the error path and converts them into {@link Problem} responses.
   *
   * @param request the current HTTP request
   * @return a {@link ResponseEntity} containing a {@link Problem} body and proper HTTP status
   * @since 1.2.0
   */
  @RequestMapping
  public ResponseEntity<Problem> error(HttpServletRequest request) {
    HttpStatus status = getStatus(request);

    if (status == HttpStatus.NO_CONTENT) {
      return ResponseEntity.noContent().build();
    }

    ProblemContext context =
        request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE) instanceof ProblemContext attribute
            ? attribute
            : ProblemContext.create();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    if (problemOperations == null) {
      Problem problem = problemPostProcessor.process(context, Problem.of(status.value()));
      return postProcessed(request, problem, headers, status);
    }

    ServletWebRequest webRequest = new ServletWebRequest(request);
    Exception ex = findException(webRequest);
    if (ex == null) {
      Problem problem = problemOperations.postProcess(context, Problem.of(status.value()));
      return postProcessed(request, problem, headers, status);
    }

    if (!problemOperations.supports(ex)) {
      log.error(
          "Unhandled exception (method={}, endpoint={}, traceId={})",
          request.getMethod(),
          request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI),
          request.getAttribute(TRACE_ID_ATTRIBUTE),
          ex);
    }

    Problem problem;
    try {
      problem = problemOperations.translate(context, ex, headers);
    } catch (RuntimeException e) {
      log.warn("Unable to resolve problem response for {}", ex.getClass().getName(), e);
      problem =
          problemOperations.postProcess(
              context, Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    HttpStatus responseStatus =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    for (ProblemInspector inspector : problemInspectors) {
      inspector.inspect(context, problem, ex, headers, responseStatus, webRequest);
    }

    return postProcessed(request, problem, headers, responseStatus);
  }

  private static ResponseEntity<Problem> postProcessed(
      HttpServletRequest request, Problem problem, HttpHeaders headers, HttpStatus status) {
    request.setAttribute(POST_PROCESSED_PROBLEM_ATTRIBUTE, problem);
    return new ResponseEntity<>(problem, headers, status);
  }

  private @Nullable Exception findException(ServletWebRequest webRequest) {
    Throwable error = errorAttributes.getError(webRequest);
    while (error instanceof ServletException && error.getClass() == ServletException.class) {
      Throwable cause = error.getCause();
      if (cause == null) {
        break;
      }
      error = cause;
    }
    return error instanceof Exception exception ? exception : null;
  }
}
