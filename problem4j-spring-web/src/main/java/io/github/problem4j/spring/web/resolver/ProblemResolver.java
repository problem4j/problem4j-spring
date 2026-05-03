/*
 * Copyright 2025-2026 The Problem4J Authors
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

package io.github.problem4j.spring.web.resolver;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Represents a resolver from a specific {@link Exception} to an immutable {@link Problem}.
 *
 * <p>Implementations are supposed to be stateless.
 *
 * @since 1.2.0
 */
public interface ProblemResolver {

  /**
   * Returns the type of {@link Exception} this resolver supports.
   *
   * @return supported exception class
   * @since 1.2.0
   */
  Class<? extends Exception> getExceptionClass();

  /**
   * Resolves the given exception into an immutable {@link Problem}.
   *
   * @param context problem context
   * @param ex exception to resolve
   * @param headers to be included in HTTP response
   * @param status HTTP status recommended by caller
   * @return an immutable {@link Problem} representing the resolved problem
   * @since 3.0.0
   */
  Problem resolve(ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status);
}
