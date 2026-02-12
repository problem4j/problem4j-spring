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

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.autoconfigure.error.AbstractErrorController;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A simple {@link org.springframework.boot.webmvc.error.ErrorController} implementation that
 * returns HTTP problems (RFC 7807) instead of HTML error pages.
 *
 * <p>It converts generic servlet errors into {@link Problem} responses with the appropriate HTTP
 * status and content type {@code application/problem+json}.
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class ProblemErrorController extends AbstractErrorController {

  private final ProblemPostProcessor problemPostProcessor;

  /**
   * Creates a new {@link ProblemErrorController}.
   *
   * @param problemPostProcessor the post-processor to use
   * @param errorAttributes the error attributes used to obtain error information
   */
  public ProblemErrorController(
      ProblemPostProcessor problemPostProcessor, ErrorAttributes errorAttributes) {
    super(errorAttributes);
    this.problemPostProcessor = problemPostProcessor;
  }

  /**
   * Handles all requests to the error path and converts them into {@link Problem} responses.
   *
   * @param request the current HTTP request
   * @return a {@link ResponseEntity} containing a {@link Problem} body and proper HTTP status
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

    Problem problem = Problem.builder().status(status.value()).build();
    problem = problemPostProcessor.process(context, problem);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PROBLEM_JSON);

    return new ResponseEntity<>(problem, headers, status);
  }
}
