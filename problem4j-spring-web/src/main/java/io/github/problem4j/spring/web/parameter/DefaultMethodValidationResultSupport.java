/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.problem4j.spring.web.parameter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;

/**
 * Default implementation of {@link MethodValidationResultSupport}.
 *
 * @since 1.2.0
 */
public class DefaultMethodValidationResultSupport implements MethodValidationResultSupport {

  private final MethodParameterSupport methodParameterSupport;

  /**
   * Uses {@link DefaultMethodParameterSupport} as the default {@link MethodParameterSupport}.
   *
   * @since 1.2.0
   */
  public DefaultMethodValidationResultSupport() {
    this(new DefaultMethodParameterSupport());
  }

  /**
   * Creates a new instance using provided {@link MethodParameterSupport}.
   *
   * @param methodParameterSupport the {@link MethodParameterSupport} implementation to use
   * @since 1.2.0
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
   * @since 1.2.0
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
