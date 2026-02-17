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

import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;

/** Default implementation of {@link MethodValidationResultSupport}. */
public class DefaultMethodValidationResultSupport implements MethodValidationResultSupport {

  private final MethodParameterSupport methodParameterSupport;

  /** Uses {@link DefaultMethodParameterSupport} as the default {@link MethodParameterSupport}. */
  public DefaultMethodValidationResultSupport() {
    this(new DefaultMethodParameterSupport());
  }

  /**
   * Creates a new instance using provided {@link MethodParameterSupport}.
   *
   * @param methodParameterSupport the {@link MethodParameterSupport} implementation to use
   */
  public DefaultMethodValidationResultSupport(MethodParameterSupport methodParameterSupport) {
    this.methodParameterSupport = methodParameterSupport;
  }

  /**
   * Builds a {@link Violation}s list from a {@link MethodValidationResult} produced by method /
   * parameter validation (e.g. {@code @Validated} on a controller). Each parameter violation is
   * mapped to a {@link Violation} whose name is the resolved method parameter name and message is
   * the constraint message.
   *
   * @param result aggregated method validation result (must not be {@code null})
   * @return list of violations extracted from the validation result
   */
  @Override
  public List<Violation> fetchViolations(MethodValidationResult result) {
    List<Violation> violations = new ArrayList<>();
    for (ParameterValidationResult valueResult : result.getValueResults()) {
      String fieldName =
          methodParameterSupport.findParameterName(valueResult.getMethodParameter()).orElse(null);
      valueResult
          .getResolvableErrors()
          .forEach(error -> violations.add(new Violation(fieldName, error.getDefaultMessage())));
    }
    return violations;
  }
}
