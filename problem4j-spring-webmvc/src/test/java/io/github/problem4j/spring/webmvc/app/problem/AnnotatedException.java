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

package io.github.problem4j.spring.webmvc.app.problem;

import io.github.problem4j.core.ProblemMapping;

@ProblemMapping(
    type = "https://example.org/annotated/{value1}",
    title = "Annotated Exception",
    status = 418,
    detail = "value2:{value2}",
    instance = "https://example.org/annotated/instance/{value3}")
public class AnnotatedException extends RuntimeException {

  private final String value1;
  private final Long value2;
  private final boolean value3;

  public AnnotatedException(String value1, Long value2, boolean value3) {
    this.value1 = value1;
    this.value2 = value2;
    this.value3 = value3;
  }

  public String getValue1() {
    return value1;
  }

  public Long getValue2() {
    return value2;
  }

  public boolean isValue3() {
    return value3;
  }
}
