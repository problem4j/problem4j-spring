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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.resolver.ProblemResolutionFacade;
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
 * status and content type {@code application/problem+json}.
 *
 * @since 1.2.0
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ProblemErrorController extends AbstractErrorController {

  private static final Logger log = LoggerFactory.getLogger(ProblemErrorController.class);

  private final ProblemPostProcessor problemPostProcessor;
  private final ErrorAttributes errorAttributes;
  private final ProblemResolutionFacade problemResolutionFacade;
  private final List<AdviceWebMvcInspector> adviceWebMvcInspectors;

  /**
   * Creates a new {@link ProblemErrorController}, rendering only the HTTP status of errors (see
   * {@link ProblemResolutionFacade#statusOnly()}).
   *
   * @param problemPostProcessor the post-processor to use
   * @param errorAttributes the error attributes used to obtain error information
   * @since 1.2.0
   */
  public ProblemErrorController(
      ProblemPostProcessor problemPostProcessor, ErrorAttributes errorAttributes) {
    this(problemPostProcessor, errorAttributes, ProblemResolutionFacade.statusOnly(), List.of());
  }

  /**
   * Creates a new {@link ProblemErrorController}, resolving exceptions that caused errors with
   * given {@link ProblemResolutionFacade}.
   *
   * @param problemPostProcessor the post-processor to use
   * @param errorAttributes the error attributes used to obtain error information
   * @param problemResolutionFacade resolves exceptions that caused errors
   * @param adviceWebMvcInspectors inspectors notified about resolved exceptions
   * @throws NullPointerException if {@code problemResolutionFacade} or {@code
   *     adviceWebMvcInspectors} is {@code null}
   * @since 3.1.0
   */
  public ProblemErrorController(
      ProblemPostProcessor problemPostProcessor,
      ErrorAttributes errorAttributes,
      ProblemResolutionFacade problemResolutionFacade,
      List<? extends AdviceWebMvcInspector> adviceWebMvcInspectors) {
    super(errorAttributes);
    this.problemPostProcessor = problemPostProcessor;
    this.errorAttributes = errorAttributes;
    this.problemResolutionFacade = problemResolutionFacade;
    this.adviceWebMvcInspectors = new ArrayList<>(adviceWebMvcInspectors);
  }

  /**
   * Handles all requests to the error path and converts them into {@link Problem} responses. If the
   * error was caused by an exception, it is resolved the same way as by exception handlers.
   * Otherwise, the response carries only the HTTP status of the error.
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

    ProblemContext context = (ProblemContext) request.getAttribute(PROBLEM_CONTEXT_ATTRIBUTE);
    if (context == null) {
      context = ProblemContext.create();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    ServletWebRequest webRequest = new ServletWebRequest(request);
    Exception ex = findException(webRequest);
    if (ex == null) {
      Problem problem = problemPostProcessor.process(context, Problem.of(status.value()));
      return new ResponseEntity<>(problem, headers, status);
    }

    Problem problem;
    try {
      problem = problemResolutionFacade.resolve(context, ex, headers, status);
      problem = problemPostProcessor.process(context, problem);
    } catch (Exception e) {
      logAdviceException(log, ex, webRequest, e);
      problem = Problem.of(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    HttpStatus resolvedStatus =
        Optional.ofNullable(HttpStatus.resolve(problem.getStatus()))
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    for (AdviceWebMvcInspector inspector : adviceWebMvcInspectors) {
      inspector.inspect(context, problem, ex, headers, resolvedStatus, webRequest);
    }
    return new ResponseEntity<>(problem, headers, resolvedStatus);
  }

  // FrameworkServlet wraps exceptions other than ServletException and IOException into a plain
  // ServletException, but DefaultErrorAttributes stores the original one before it is wrapped
  private @Nullable Exception findException(ServletWebRequest webRequest) {
    Throwable error = errorAttributes.getError(webRequest);
    if (error != null
        && error.getClass() == ServletException.class
        && error.getCause() instanceof Exception cause) {
      return cause;
    }
    return error instanceof Exception ex ? ex : null;
  }
}
