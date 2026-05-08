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

package io.github.problem4j.spring.web.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

class ErrorResponseProblemResolverTest {

  private ErrorResponseProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ErrorResponseProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsErrorResponseException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ErrorResponseException.class);
  }

  @Test
  void givenErrorResponseException_whenResolve_thenCopiesStatusFromException() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.CONFLICT);

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.OK)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
  }

  @Test
  void givenErrorResponseExceptionWithDetail_whenResolve_thenCopiesDetailFromBody() {
    ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "invalid input");
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.BAD_REQUEST, body, null);

    Problem problem =
        resolver
            .resolveBuilder(
                ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).isEqualTo("invalid input");
  }

  @Test
  void givenErrorResponseException_whenResolve_thenIgnoresPassedStatus() {
    ErrorResponseException ex = new ErrorResponseException(HttpStatus.FORBIDDEN);

    Problem problem =
        resolver
            .resolveBuilder(
                ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }
}
