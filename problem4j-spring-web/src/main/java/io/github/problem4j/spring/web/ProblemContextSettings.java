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

/**
 * Settings used when building a {@code ProblemContext} for incoming requests.
 *
 * <p>Provides access to infrastructure configuration such as the HTTP header name that carries a
 * trace identifier. Implementations are typically backed by external configuration (e.g. Spring
 * Boot properties).
 *
 * @see io.github.problem4j.core.ProblemContext
 */
public interface ProblemContextSettings {

  /**
   * Returns the name of the HTTP header that contains a trace / correlation ID.
   *
   * <p>The trace ID (if present) may be injected into generated Problem responses (e.g. via
   * placeholders in instance/type override templates) and echoed back to clients to aid in log
   * correlation and diagnostics.
   *
   * @return the tracing header name, or {@code null} if tracing is disabled / not configured
   */
  String getTracingHeaderName();
}
