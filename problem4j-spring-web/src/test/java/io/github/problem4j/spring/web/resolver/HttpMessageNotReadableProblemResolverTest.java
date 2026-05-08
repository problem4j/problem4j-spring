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
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

class HttpMessageNotReadableProblemResolverTest {

  private HttpMessageNotReadableProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new HttpMessageNotReadableProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsHttpMessageNotReadableException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(HttpMessageNotReadableException.class);
  }

  @Test
  void givenExceptionWithoutJacksonCause_whenResolve_thenReturnsBadRequestProblem() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("msg", new MockHttpInputMessage(new byte[0]));

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.BAD_REQUEST)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }

  @Test
  void givenExceptionWithoutJacksonCause_whenResolve_thenIgnoresPassedStatus() {
    HttpMessageNotReadableException ex =
        new HttpMessageNotReadableException("msg", new MockHttpInputMessage(new byte[0]));

    Problem problem =
        resolver
            .resolveBuilder(
                ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
  }
}
