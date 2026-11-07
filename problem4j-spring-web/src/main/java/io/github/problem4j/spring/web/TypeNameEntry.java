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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Custom mapping of a Java type to a name. Used primarily by {@link SimpleTypeNameMapper}.
 *
 * @since 3.1.0
 */
public final class TypeNameEntry {

  private final Class<?> type;
  private final String name;

  /**
   * Creates a new {@link TypeNameEntry}.
   *
   * @param type the Java type to map, matching also its subtypes
   * @param name the name the type is mapped to
   * @throws NullPointerException if {@code type} or {@code name} is {@code null}
   * @since 3.1.0
   */
  public TypeNameEntry(Class<?> type, String name) {
    this.type = requireNonNull(type, "type must not be null");
    this.name = requireNonNull(name, "name must not be null");
  }

  /**
   * Returns the Java type this entry maps.
   *
   * @return the mapped Java type
   * @since 3.1.0
   */
  public Class<?> getType() {
    return type;
  }

  /**
   * Returns the name the type is mapped to.
   *
   * @return the type name
   * @since 3.1.0
   */
  public String getName() {
    return name;
  }

  /**
   * Compares this entry to another object for equality. Two entries are equal if their types and
   * names are equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if both entries are equal, {@code false} otherwise
   * @since 3.1.0
   */
  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TypeNameEntry that)) {
      return false;
    }
    return type.equals(that.type) && name.equals(that.name);
  }

  /**
   * Returns a hash code consistent with {@link #equals(Object)}.
   *
   * @return the hash code
   * @since 3.1.0
   */
  @Override
  public int hashCode() {
    return Objects.hash(type, name);
  }

  /**
   * Returns a string representation of this entry, including the type and name.
   *
   * @return the string representation
   * @since 3.1.0
   */
  @Override
  public String toString() {
    return "TypeNameEntry[type=" + type.getName() + ", name=" + name + "]";
  }
}
