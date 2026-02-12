/*
 * Copyright (c) 2025-2026 Damian Malczewski
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

import java.util.Optional;
import org.springframework.core.MethodParameter;

/** Marker interface for method parameter support in problem4j-spring-web. */
public interface MethodParameterSupport {

  /**
   * Resolve a stable logical name for a method parameter, honoring supported Spring binding
   * annotations. If an annotation supplies an explicit {@code name} or {@code value}, that wins;
   * otherwise falls back to the parameter's discovered name. Unknown or unsupported annotations are
   * ignored.
   *
   * @param parameter Spring {@link MethodParameter} (may be {@code null})
   * @return optional parameter name; empty if the input is {@code null}
   */
  Optional<String> findParameterName(MethodParameter parameter);
}
