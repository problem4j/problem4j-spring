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

package io.github.problem4j.spring.web;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of {@link ClassDistanceEvaluation} that calculates inheritance distance using a
 * recursive graph-based approach. Supports limiting the maximum depth to prevent stack overflow in
 * deep or complex hierarchies.
 */
public class GraphClassDistanceEvaluation implements ClassDistanceEvaluation {

  private final int defaultMaxDepth;

  private final boolean superclassIncluded;
  private final boolean interfacesIncluded;

  /**
   * Creates a {@link GraphClassDistanceEvaluation} using the default maximum depth and the given
   * traversal modes.
   *
   * <p>Traversal modes define which parts of the inheritance hierarchy may be followed during
   * distance calculation. If no modes are provided, no traversal will be performed.
   *
   * @param modes the traversal modes to enable
   */
  public GraphClassDistanceEvaluation(HierarchyTraversalMode... modes) {
    this(Set.of(modes));
  }

  /**
   * Creates a {@link GraphClassDistanceEvaluation} using the default maximum depth and the given
   * collection of traversal modes.
   *
   * <p>The collection defines which parts of the inheritance hierarchy should be explored during
   * distance calculation. If the collection is empty, both superclass and interface traversal are
   * enabled by default.
   *
   * @param modes the traversal modes to enable
   */
  public GraphClassDistanceEvaluation(Collection<HierarchyTraversalMode> modes) {
    this(DEFAULT_MAX_DEPTH, modes);
  }

  /**
   * Creates a {@link GraphClassDistanceEvaluation} with a custom maximum depth and the given
   * traversal modes.
   *
   * <p>Traversal modes define which hierarchy paths may be traversed, such as superclasses or
   * interfaces. The evaluator will only explore the paths enabled by these modes.
   *
   * @param defaultMaxDepth the maximum depth to use for inheritance calculations
   * @param modes the traversal modes to enable
   */
  public GraphClassDistanceEvaluation(int defaultMaxDepth, HierarchyTraversalMode... modes) {
    this(defaultMaxDepth, Set.of(modes));
  }

  /**
   * Creates a {@link GraphClassDistanceEvaluation} with a custom maximum depth and a list of
   * traversal modes.
   *
   * <p>The collection defines which parts of the inheritance hierarchy should be explored during
   * distance calculation. If the collection is empty, both superclass and interface traversal are
   * enabled by default.
   *
   * @param defaultMaxDepth the maximum depth to use for inheritance calculations
   * @param modes the traversal modes to enable
   */
  public GraphClassDistanceEvaluation(
      int defaultMaxDepth, Collection<HierarchyTraversalMode> modes) {
    this.defaultMaxDepth = defaultMaxDepth;

    superclassIncluded = modes.isEmpty() || modes.contains(HierarchyTraversalMode.SUPERCLASS);
    interfacesIncluded = modes.isEmpty() || modes.contains(HierarchyTraversalMode.INTERFACES);
  }

  /**
   * Calculates the inheritance distance between two class types.
   *
   * <p>The distance represents how far target type is from the base type in the class hierarchy. If
   * the types are not compatible, {@link Integer#MAX_VALUE} is returned.
   *
   * @param target the class whose distance is being measured
   * @param base the class to measure distance to
   * @param maxDepth the maximum depth to traverse in the inheritance hierarchy
   * @return number of superclass steps between the two types, or {@link Integer#MAX_VALUE} if not
   *     assignable
   */
  @Override
  public int calculate(Class<?> target, Class<?> base, int maxDepth) {
    return calculateInternal(target, base, 0, maxDepth);
  }

  /**
   * Returns the default maximum depth used when calculating inheritance distance.
   *
   * <p>This limit helps prevent stack overflow when traversing deep or complex class/interface
   * hierarchies.
   */
  @Override
  public int getDefaultMaxDepth() {
    return defaultMaxDepth;
  }

  private int calculateInternal(Class<?> target, Class<?> base, int currentDepth, int maxDepth) {
    if (currentDepth > maxDepth || Objects.equals(target, base)) {
      return 0;
    }

    if (!base.isAssignableFrom(target)) {
      return Integer.MAX_VALUE;
    }

    int minDistance = Integer.MAX_VALUE;

    if (superclassIncluded) {
      Class<?> superclass = target.getSuperclass();
      if (superclass != null) {
        int distance = calculateInternal(superclass, base, currentDepth + 1, maxDepth);
        if (distance != Integer.MAX_VALUE) {
          minDistance = distance + 1;
        }
      }
    }

    if (interfacesIncluded) {
      for (Class<?> iface : target.getInterfaces()) {
        int distance = calculateInternal(iface, base, currentDepth + 1, maxDepth);
        if (distance != Integer.MAX_VALUE) {
          minDistance = Math.min(minDistance, distance + 1);
        }
      }
    }

    return minDistance;
  }
}
