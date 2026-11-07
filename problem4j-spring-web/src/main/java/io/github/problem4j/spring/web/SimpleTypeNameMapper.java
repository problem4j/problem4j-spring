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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of {@link TypeNameMapper} that maps simple Java types to common, human-readable
 * type names.
 *
 * @since 1.2.7
 */
public class SimpleTypeNameMapper implements TypeNameMapper {

  /**
   * Creates a new {@link SimpleTypeNameMapper}.
   *
   * @since 1.2.7
   */
  public SimpleTypeNameMapper() {}

  /**
   * Common type name for all integer types.
   *
   * @since 1.2.7
   */
  protected static final String INTEGER_TYPE = "integer";

  /**
   * Common type name for all decimal types.
   *
   * @since 1.2.7
   */
  protected static final String NUMBER_TYPE = "number";

  /**
   * Common type name for all boolean types.
   *
   * @since 1.2.7
   */
  protected static final String BOOLEAN_TYPE = "boolean";

  /**
   * Common type name for string types.
   *
   * @since 1.2.7
   */
  protected static final String STRING_TYPE = "string";

  /**
   * Common type name for enum types.
   *
   * @since 1.2.7
   */
  protected static final String ENUM_TYPE = "enum";

  /**
   * Common type name for array and collection types.
   *
   * @since 1.2.7
   */
  protected static final String ARRAY_TYPE = "array";

  /**
   * Maps a Java type to a common type name. Supported mappings include:
   *
   * <ul>
   *   <li>Integer types ({@code int}, {@code long}, {@code short}, {@code byte}, their wrapper
   *       classes, {@code BigInteger}, {@code AtomicInteger} and {@code AtomicLong}) to {@code
   *       "integer"}
   *   <li>Decimal types ({@code double}, {@code float}, their wrapper classes and {@code
   *       BigDecimal} ) to {@code "number"}
   *   <li>Boolean types ({@code boolean} and {@code Boolean}) to {@code "boolean"}
   *   <li>String type ({@code String}) to {@code "string"}
   *   <li>Enum types to {@code "enum"}
   *   <li>{@code Array} and {@code Collection} types to {@code "array"}
   * </ul>
   *
   * @param type the Java type to map
   * @return an {@code Optional} containing the mapped string name, or empty if the type cannot be
   *     mapped
   * @since 1.2.7
   */
  @Override
  public Optional<String> map(@Nullable Class<?> type) {
    if (type == null) {
      return Optional.empty();
    }
    if (isInteger(type)) {
      return Optional.of(INTEGER_TYPE);
    } else if (isDecimal(type)) {
      return Optional.of(NUMBER_TYPE);
    } else if (isBoolean(type)) {
      return Optional.of(BOOLEAN_TYPE);
    } else if (isEnum(type)) {
      return Optional.of(ENUM_TYPE);
    } else if (isString(type)) {
      return Optional.of(STRING_TYPE);
    } else if (isArray(type)) {
      return Optional.of(ARRAY_TYPE);
    }
    return Optional.empty();
  }

  /**
   * Determines if the given class represents an integer type.
   *
   * @param clazz the class to check
   * @return {@code true} if the class is an integer type, {@code false} otherwise
   * @since 1.2.7
   */
  protected boolean isInteger(Class<?> clazz) {
    return clazz == int.class
        || clazz == Integer.class
        || clazz == long.class
        || clazz == Long.class
        || clazz == BigInteger.class
        || clazz == AtomicLong.class
        || clazz == AtomicInteger.class
        || clazz == short.class
        || clazz == Short.class
        || clazz == byte.class
        || clazz == Byte.class;
  }

  /**
   * Determines if the given class represents a decimal type.
   *
   * @param clazz the class to check
   * @return {@code true} if the class is a decimal type, {@code false} otherwise
   * @since 1.2.7
   */
  protected boolean isDecimal(Class<?> clazz) {
    return clazz == double.class
        || clazz == Double.class
        || clazz == BigDecimal.class
        || clazz == float.class
        || clazz == Float.class;
  }

  /**
   * Determines if the given class represents a boolean type.
   *
   * @param clazz the class to check
   * @return {@code true} if the class is a boolean type, {@code false} otherwise
   * @since 1.2.7
   */
  protected boolean isBoolean(Class<?> clazz) {
    return clazz == boolean.class || clazz == Boolean.class;
  }

  /**
   * Determines if the given class represents an enum type.
   *
   * @param type the class to check
   * @return {@code true} if the class is an enum type, {@code false} otherwise
   * @since 1.2.8
   */
  protected boolean isEnum(Class<?> type) {
    return type.isEnum();
  }

  /**
   * Determines if the given class represents a string type.
   *
   * @param type the class to check
   * @return {@code true} if the class is a string type, {@code false} otherwise
   * @since 1.2.7
   */
  protected boolean isString(Class<?> type) {
    return type == String.class;
  }

  /**
   * Determines if the given type is an array or a collection.
   *
   * @param type the class to check
   * @return {@code true} if the type is an array or a collection, {@code false} otherwise
   * @since 1.2.7
   */
  protected boolean isArray(Class<?> type) {
    return type.isArray() || Collection.class.isAssignableFrom(type);
  }
}
