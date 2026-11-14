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
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.core.ProblemException;
import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import io.github.problem4j.spring.web.operations.ProblemTemplate;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class ProblemErrorControllerTest {

  private static final ProblemPostProcessor TRACING =
      (context, problem) ->
          problem.toBuilder()
              .extension("traceId", context != null ? context.get("traceId") : "no-context")
              .build();

  private final ProblemOperations operations =
      ProblemTemplate.builder().problemPostProcessor(TRACING).build();

  private MockHttpServletRequest request;
  private List<Exception> inspected;

  @BeforeEach
  void beforeEach() {
    request = new MockHttpServletRequest("GET", "/error");
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/test");
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
    inspected = new ArrayList<>();
  }

  private ProblemErrorController deferred(ProblemOperations operations) {
    return new ProblemErrorController(
        operations,
        new DefaultErrorAttributes(),
        List.of((context, problem, ex, headers, status, webRequest) -> inspected.add(ex)));
  }

  private static Problem body(ResponseEntity<Problem> response) {
    return requireNonNull(response.getBody());
  }

  @Test
  void givenStatusOnlyController_whenHandlingError_thenPostProcessedProblemOfStatus() {
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
    request.setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", "abc"));
    ProblemErrorController controller =
        new ProblemErrorController(TRACING, new DefaultErrorAttributes());

    ResponseEntity<Problem> response = controller.error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(body(response).getStatus()).isEqualTo(404);
    assertThat(body(response).getExtensions()).containsEntry("traceId", "abc");
  }

  @Test
  void givenStatusOnlyControllerAndException_whenHandlingError_thenExceptionIsIgnored() {
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ProblemException(Problem.of(409)));
    ProblemErrorController controller =
        new ProblemErrorController(TRACING, new DefaultErrorAttributes());

    assertThat(body(controller.error(request)).getStatus()).isEqualTo(500);
  }

  @Test
  void givenNoContentStatus_whenHandlingError_thenEmptyResponse() {
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 204);

    ResponseEntity<Problem> response = deferred(operations).error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(response.getBody()).isNull();
  }

  @Test
  void givenSendErrorWithoutException_whenHandlingError_thenPostProcessedProblemOfStatus() {
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
    request.setAttribute(PROBLEM_CONTEXT_ATTRIBUTE, ProblemContext.create().put("traceId", "abc"));

    ResponseEntity<Problem> response = deferred(operations).error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(body(response).getExtensions()).containsEntry("traceId", "abc");
    assertThat(inspected).isEmpty();
  }

  @Test
  void givenKnownException_whenHandlingError_thenTranslatedAndInspected() {
    ProblemException ex = new ProblemException(Problem.of(409, "conflict"));
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);

    ResponseEntity<Problem> response = deferred(operations).error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(body(response).getDetail()).isEqualTo("conflict");
    assertThat(inspected).containsExactly(ex);
  }

  @Test
  void givenExceptionWrappedInServletException_whenHandlingError_thenCauseIsTranslated() {
    ProblemException cause = new ProblemException(Problem.of(409));
    request.setAttribute(
        RequestDispatcher.ERROR_EXCEPTION,
        new ServletException("wrapper", new ServletException("nested", cause)));

    assertThat(body(deferred(operations).error(request)).getStatus()).isEqualTo(409);
    assertThat(inspected).containsExactly(cause);
  }

  @Test
  void givenServletExceptionWithoutCause_whenHandlingError_thenItIsTranslated() {
    ServletException ex = new ServletException("no cause");
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);

    assertThat(body(deferred(operations).error(request)).getStatus()).isEqualTo(500);
    assertThat(inspected).containsExactly(ex);
  }

  @Test
  void givenErrorThrowable_whenHandlingError_thenProblemOfStatus() {
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new AssertionError("not an exception"));

    assertThat(body(deferred(operations).error(request)).getStatus()).isEqualTo(500);
    assertThat(inspected).isEmpty();
  }

  @Test
  void givenUnknownException_whenHandlingError_thenInternalServerErrorProblem() {
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException("unknown"));

    ResponseEntity<Problem> response = deferred(operations).error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(inspected).hasSize(1);
  }

  @Test
  void givenProblemWithUnknownStatus_whenHandlingError_thenInternalServerErrorStatus() {
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new ProblemException(Problem.of(599)));

    assertThat(deferred(operations).error(request).getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void givenFailingTranslation_whenHandlingError_thenPostProcessedInternalServerError() {
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new IllegalStateException());

    ResponseEntity<Problem> response = deferred(new FailingOperations()).error(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(body(response).getExtensions()).containsEntry("processed", true);
  }

  @Test
  void givenServletExceptionSubclass_whenHandlingError_thenItIsNotUnwrapped() {
    CustomServletException ex = new CustomServletException(new ProblemException(Problem.of(409)));
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, ex);

    assertThat(body(deferred(operations).error(request)).getStatus()).isEqualTo(500);
    assertThat(inspected).containsExactly(ex);
  }

  static class CustomServletException extends ServletException {

    CustomServletException(Throwable cause) {
      super("custom", cause);
    }
  }

  static class FailingOperations implements ProblemOperations {

    @Override
    public boolean supports(Exception ex) {
      return true;
    }

    @Override
    public Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem resolve(
        ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
      throw new IllegalStateException("broken");
    }

    @Override
    public Problem postProcess(@Nullable ProblemContext context, Problem problem) {
      return problem.toBuilder().extension("processed", true).build();
    }

    @Override
    public ProblemDetail postProcess(@Nullable ProblemContext context, ProblemDetail detail) {
      return detail;
    }
  }
}
