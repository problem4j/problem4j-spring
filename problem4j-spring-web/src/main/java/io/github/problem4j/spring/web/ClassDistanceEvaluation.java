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
package io.github.problem4j.spring.web;

/**
 * Strategy for calculating the inheritance distance between two Java classes or interfaces.
 *
 * <p>The distance counts the number of steps in the class and interface hierarchy from a target
 * class to a base class. Both superclass chains and implemented or extended interfaces are
 * considered, and the minimal distance is returned if multiple paths exist.
 *
 * <p>Methods allow specifying a maximum depth to avoid stack overflow, or use a default depth.
 */
public interface ClassDistanceEvaluation {

  /** Default maximum depth for class distance evaluation. */
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
   */
  default int getDefaultMaxDepth() {
    return DEFAULT_MAX_DEPTH;
  }
}
