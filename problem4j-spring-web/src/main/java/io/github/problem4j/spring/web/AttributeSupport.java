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

package io.github.problem4j.spring.web;

/**
 * Utility class providing constants and helper methods for tracing support within the Problem4J.
 *
 * @since 1.2.0
 */
public final class AttributeSupport {

  /**
   * Request attribute key used to store a trace identifier.
   *
   * <p>This key is <b>not used in {@code ProblemContext}</b>; it is used exclusively as a request
   * attribute (WebFlux or WebMVC) and is assigned by framework-specific filters.
   *
   * @see io.github.problem4j.core.ProblemContext
   * @since 1.2.0
   */
  public static final String TRACE_ID_ATTRIBUTE = "io.github.problem4j.spring.web.traceId";

  /**
   * Request attribute key used to store the object associated with the current request. It allows
   * sharing contextual information (such as trace identifiers or additional diagnostic data)
   * between components involved in problem handling.
   *
   * @since 1.2.0
   */
  public static final String PROBLEM_CONTEXT_ATTRIBUTE = "io.github.problem4j.core.ProblemContext";

  private AttributeSupport() {}
}
