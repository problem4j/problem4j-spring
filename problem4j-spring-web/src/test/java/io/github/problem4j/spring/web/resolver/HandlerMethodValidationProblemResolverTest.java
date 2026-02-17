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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class HandlerMethodValidationProblemResolverTest {

  private HandlerMethodValidationProblemResolver handlerMethodValidationProblemResolver;

  @BeforeEach
  void beforeEach() {
    handlerMethodValidationProblemResolver = new HandlerMethodValidationProblemResolver();
  }

  @Test
  void givenHandlerMethodValidationException_shouldGenerateProblem() {
    MethodValidationResult mockMethodValidationResult = mock(MethodValidationResult.class);
    HandlerMethodValidationException ex =
        new HandlerMethodValidationException(mockMethodValidationResult);

    Problem problem =
        handlerMethodValidationProblemResolver.resolveProblem(
            ProblemContext.create().put("traceId", "traceId"),
            ex,
            new HttpHeaders(),
            ex.getStatusCode());

    assertEquals(Problem.BLANK_TYPE, problem.getType());
    assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), problem.getTitle());
    assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
  }
}
