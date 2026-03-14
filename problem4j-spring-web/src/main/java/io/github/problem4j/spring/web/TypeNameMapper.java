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

import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Maps a Java type to a string name. Used to hide implementation details of the Java type system
 * from the API consumer. For example, both {@code String[]} {@code List<String>} might be mapped to
 * {@code "array"}, as JSON does not distinguish between different collection types.
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
   */
  Optional<String> map(@Nullable Class<?> type);
}
