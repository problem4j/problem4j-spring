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
import org.springframework.web.server.ResponseStatusException;

class ResponseStatusProblemResolverTest {

  private ResponseStatusProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new ResponseStatusProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsResponseStatusException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(ResponseStatusException.class);
  }

  @Test
  void givenResponseStatusException_whenResolve_thenCopiesStatusFromException() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND);

    Problem problem =
        resolver
            .resolveBuilder(
                ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  void givenResponseStatusExceptionWithCustomStatus_whenResolve_thenUsesExceptionStatus() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.OK)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.PAYMENT_REQUIRED.value());
  }
}
