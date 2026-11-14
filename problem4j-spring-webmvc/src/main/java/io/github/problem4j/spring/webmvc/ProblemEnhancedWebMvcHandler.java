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

package io.github.problem4j.spring.webmvc;

import io.github.problem4j.spring.web.ProblemPostProcessor;
import io.github.problem4j.spring.web.ProblemResolverStore;
import io.github.problem4j.spring.web.operations.ProblemOperations;
import java.util.List;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Former name of {@link ProblemResponseEntityExceptionHandler}, kept for backwards compatibility.
 *
 * <p>Part of the advice path, registered with {@code problem4j.exception-handling.mode=advice}
 * (default) under this former name.
 *
 * @since 1.2.0
 * @deprecated since 3.1.0 in favor of {@link ProblemResponseEntityExceptionHandler}, which this
 *     class extends
 */
@Deprecated(since = "3.1.0", forRemoval = true)
@RestControllerAdvice
public class ProblemEnhancedWebMvcHandler extends ProblemResponseEntityExceptionHandler {

  /**
   * Creates a new {@link ProblemEnhancedWebMvcHandler}.
   *
   * @param problemResolverStore the resolver store
   * @param problemPostProcessor the post-processor
   * @param problemInspectors the inspectors
   * @since 1.2.0
   */
  @SuppressWarnings("removal")
  public ProblemEnhancedWebMvcHandler(
      ProblemResolverStore problemResolverStore,
      ProblemPostProcessor problemPostProcessor,
      List<? extends ProblemInspector> problemInspectors) {
    super(problemResolverStore, problemPostProcessor, problemInspectors);
  }

  /**
   * Creates a new {@link ProblemEnhancedWebMvcHandler}, resolving and post-processing exceptions
   * with {@link ProblemOperations}.
   *
   * @param problemOperations the operations resolving and post-processing problems
   * @param problemInspectors the inspectors
   * @since 3.1.0
   */
  public ProblemEnhancedWebMvcHandler(
      ProblemOperations problemOperations, List<? extends ProblemInspector> problemInspectors) {
    super(problemOperations, problemInspectors);
  }
}
