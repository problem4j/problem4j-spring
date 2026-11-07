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
 * Defines which parts of the class hierarchy are traversed by {@link GraphClassDistanceEvaluation}
 * during inheritance distance calculation.
 *
 * <ul>
 *   <li>{@link #SUPERCLASS} - traverse superclasses via {@link Class#getSuperclass()}
 *   <li>{@link #INTERFACES} - traverse implemented interfaces via {@link Class#getInterfaces()}
 * </ul>
 *
 * <p>Multiple modes can be combined to control the paths explored. If no modes are specified, both
 * are considered enabled by default.
 *
 * @since 1.2.0
 */
public enum HierarchyTraversalMode {

  /**
   * Follow the superclass chain when calculating distance.
   *
   * @since 1.2.0
   */
  SUPERCLASS,

  /**
   * Follow implemented interfaces when calculating distance.
   *
   * @since 1.2.0
   */
  INTERFACES
}
