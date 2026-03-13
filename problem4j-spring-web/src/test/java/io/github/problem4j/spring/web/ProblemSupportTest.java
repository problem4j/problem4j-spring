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

package io.github.problem4j.spring.web;

import static io.github.problem4j.spring.web.ProblemSupport.resolveStatus;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Test class for {@link ProblemSupport}. Warnings are suppressed since deprecated features also
 * deserve testing.
 */
@SuppressWarnings("removal")
class ProblemSupportTest {

  @Test
  void givenNullStatus_whenResolveStatus_thenReturnsInternalServerError() {
    assertThat(resolveStatus((HttpStatusCode) null).getStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }

  @Test
  void givenKnownStatus_whenResolveStatus_thenReturnsMatchingProblemStatus() {
    assertThat(resolveStatus(HttpStatusCode.valueOf(404)).getStatus())
        .isEqualTo(HttpStatus.NOT_FOUND.value());
  }
}
