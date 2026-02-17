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

package io.github.problem4j.spring.web.parameter;

import java.util.List;
import org.springframework.validation.BindingResult;

/** Support for converting Spring {@link BindingResult}s into {@link Violation}s list. */
public interface BindingResultSupport {

  /**
   * Builds a {@link Violation}s list from a Spring {@link BindingResult} (e.g. produced when
   * binding a {@code @ModelAttribute} fails or when {@code @Valid} detects field / global errors).
   * Field errors are translated into {@link Violation}s keyed by field name; global errors use
   * {@code null} as the field name.
   *
   * @param result the binding/validation result to convert (must not be {@code null})
   * @return list of violations extracted from the binding result
   */
  List<Violation> fetchViolations(BindingResult result);
}
