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
 * Strategy for calculating the inheritance distance between two Java classes or interfaces.
 *
 * <p>The distance counts the number of steps in the class and interface hierarchy from a target
 * class to a base class. Both superclass chains and implemented or extended interfaces are
 * considered, and the minimal distance is returned if multiple paths exist.
 *
 * <p>Methods allow specifying a maximum depth to avoid stack overflow, or use a default depth.
 *
 * @since 1.2.0
 */
public interface ClassDistanceEvaluation {

  /**
   * Default maximum depth for class distance evaluation.
   *
   * @since 1.2.0
   */
  int DEFAULT_MAX_DEPTH = 50;

  /**
   * Calculates the inheritance distance between two class types.
   *
   * <p>The distance represents how far target type is from the base type in the class hierarchy.
   *
   * @param target the class whose distance is being measured
   * @param base the class to measure distance to
   * @param maxDepth the maximum depth to traverse in the inheritance hierarchy
   * @return number of superclass steps between the two types
   * @since 1.2.0
   */
  int calculate(Class<?> target, Class<?> base, int maxDepth);

  /**
   * Calculates the inheritance distance between two class types.
   *
   * <p>The distance represents how far target type is from the base type in the class hierarchy.
   *
   * @param target the class whose distance is being measured
   * @param base the class to measure distance to
   * @return number of superclass steps between the two types
   * @since 1.2.0
   */
  default int calculate(Class<?> target, Class<?> base) {
    return calculate(target, base, getDefaultMaxDepth());
  }

  /**
   * Returns the default maximum depth used when calculating inheritance distance.
   *
   * <p>This limit helps prevent stack overflow when traversing deep or complex class/interface
   * hierarchies.
   *
   * @return the default maximum depth
   * @since 1.2.0
   */
  default int getDefaultMaxDepth() {
    return DEFAULT_MAX_DEPTH;
  }
}
