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

package io.github.problem4j.spring.web;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Maps a Java type to a string name. Used to hide implementation details of the Java type system
 * from the API consumer. For example, both {@code String[]} {@code List<String>} might be mapped to
 * {@code "array"}, as JSON does not distinguish between different collection types.
 *
 * @since 1.2.7
 */
public interface TypeNameMapper {

  /**
   * Maps the given Java type to a string name. In terms of unknown type mapping, it will return an
   * empty {@code Optional} and it's up to the caller to decide how to handle it (e.g. return a
   * default name, throw an exception or not return type information at all).
   *
   * @param type the Java type to map
   * @return an {@code Optional} containing the mapped string name, or empty if the type cannot be
   *     mapped
   * @since 1.2.7
   */
  Optional<String> map(@Nullable Class<?> type);
}
