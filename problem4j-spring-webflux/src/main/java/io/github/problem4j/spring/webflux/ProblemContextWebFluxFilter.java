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

package io.github.problem4j.spring.webflux;

import io.github.problem4j.spring.web.ProblemContextSettings;

/**
 * Former name of {@link ProblemContextWebFilter}, kept for backwards compatibility.
 *
 * @since 1.2.0
 * @deprecated since 3.1.0 in favor of {@link ProblemContextWebFilter}, which this class extends
 */
@Deprecated(since = "3.1.0", forRemoval = true)
public class ProblemContextWebFluxFilter extends ProblemContextWebFilter {

  /**
   * Creates a new instance of the filter.
   *
   * @param settings configuration properties for problem context and tracing
   * @since 1.2.0
   */
  public ProblemContextWebFluxFilter(ProblemContextSettings settings) {
    super(settings);
  }
}
