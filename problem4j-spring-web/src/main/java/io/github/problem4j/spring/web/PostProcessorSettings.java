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
 * Defines configuration settings used by {@link ProblemPostProcessor} implementations to control
 * how problem responses are modified before being returned to the client.
 *
 * <p>Implementations of this interface typically provide values from application configuration (for
 * example, {@code problem4j.type-override} and {@code problem4j.instance-override}) and may include
 * runtime placeholders that are resolved during post-processing.
 *
 * <p>These settings allow applications to dynamically customize problem types and instances to
 * match organizational or tracing conventions.
 */
public interface PostProcessorSettings {

  /**
   * Returns the configured override template for the {@code type} field of a problem.
   *
   * <p>The value may include placeholders such as {@code {problem.type}}, which will be replaced at
   * runtime.
   *
   * @return the configured type override template, or {@code null} if not set
   */
  String getTypeOverride();

  /**
   * Returns the configured override template for the {@code instance} field of a problem.
   *
   * <p>The value may include placeholders such as {@code {problem.instance}} or {@code
   * {context.traceId}}, which will be replaced at runtime.
   *
   * @return the configured instance override template, or {@code null} if not set
   */
  String getInstanceOverride();
}
