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

package io.github.problem4j.spring.webmvc.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class NoResourceFoundProblemResolverTest {

  private NoResourceFoundProblemResolver resolver;

  @BeforeEach
  void beforeEach() {
    resolver = new NoResourceFoundProblemResolver();
  }

  @Test
  void givenDefaultConstructor_whenGetExceptionClass_thenReturnsNoResourceFoundException() {
    assertThat(resolver.getExceptionClass()).isEqualTo(NoResourceFoundException.class);
  }

  @Test
  void givenNoResourceFoundException_whenResolve_thenReturnsNotFoundProblem() {
    NoResourceFoundException ex =
        new NoResourceFoundException(HttpMethod.GET, "/context/missing.html", "/missing.html");

    Problem problem =
        resolver
            .resolveBuilder(ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.NOT_FOUND)
            .build();

    assertThat(problem).isEqualTo(Problem.builder().status(HttpStatus.NOT_FOUND.value()).build());
  }

  @Test
  void givenNoResourceFoundException_whenResolve_thenIgnoresPassedStatus() {
    NoResourceFoundException ex =
        new NoResourceFoundException(HttpMethod.GET, "/context/missing.html", "/missing.html");

    Problem problem =
        resolver
            .resolveBuilder(
                ProblemContext.create(), ex, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
            .build();

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
