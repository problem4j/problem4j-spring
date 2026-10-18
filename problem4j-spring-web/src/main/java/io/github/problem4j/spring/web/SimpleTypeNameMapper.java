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

import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of {@link TypeNameMapper} that maps simple Java types to common, human-readable
 * type names. Default mappings are:
 *
 * <ul>
 *   <li>Integer types ({@code int}, {@code long}, {@code short}, {@code byte}, their wrapper
 *       classes, {@code BigInteger}, {@code AtomicInteger} and {@code AtomicLong}) to {@code
 *       "integer"}
 *   <li>Decimal types ({@code double}, {@code float}, their wrapper classes and {@code BigDecimal})
 *       to {@code "number"}
 *   <li>Boolean types ({@code boolean} and {@code Boolean}) to {@code "boolean"}
 *   <li>String type ({@code String}) to {@code "string"}
 *   <li>Enum types to {@code "enum"}
 *   <li>{@code Array} and {@code Collection} types to {@code "array"}
 * </ul>
 *
 * <p>Additional types can be supported by passing custom mappings ({@link TypeNameEntry}) to one of
 * the constructors. Custom mappings are consulted before the default ones and match subtypes of
 * their types too, so a mapping for a broad type hides default mappings of its subtypes (e.g. a
 * mapping for {@code Number.class} applies to {@code Integer} and {@code BigDecimal} instead of
 * {@code "integer"} and {@code "number"}, and a mapping for {@code Object.class} replaces all
 * default mappings).
 *
 * @since 1.2.7
 */
public class SimpleTypeNameMapper implements TypeNameMapper {

  /**
   * Custom mappings of Java types to names, consulted by {@link #map(Class)} before the default
   * mappings. Iteration order is the order of comparison - the first entry whose type is the given
   * type or its supertype wins.
   *
   * @since 3.1.0
   */
  protected final Map<Class<?>, String> mappings = new LinkedHashMap<>();

  /**
   * Creates a new {@link SimpleTypeNameMapper}.
   *
   * @since 1.2.7
   */
  public SimpleTypeNameMapper() {}

  /**
   * Creates a new {@link SimpleTypeNameMapper} with a single custom mapping, taking precedence over
   * the {@linkplain SimpleTypeNameMapper default ones}. The custom mapping is matched the same way
   * as in {@link #SimpleTypeNameMapper(TypeNameEntry...)}.
   *
   * @param entry type and name it is mapped to
   * @throws NullPointerException if {@code entry} is {@code null}
   * @since 3.1.0
   */
  public SimpleTypeNameMapper(TypeNameEntry entry) {
    this(List.of(requireNonNull(entry, "entry must not be null")));
  }

  /**
   * Creates a new {@link SimpleTypeNameMapper} with custom mappings, taking precedence over the
   * {@linkplain SimpleTypeNameMapper default ones}.
   *
   * <p>Custom mappings are compared in given order and the first one matching wins. A mapping
   * matches its own type and all of its subtypes, so mappings for more specific types should be
   * given before mappings for their supertypes. Primitive types also match mappings for their
   * wrappers (e.g. a mapping for {@code Integer.class} matches {@code int.class}). If the same type
   * is given more than once, the last name wins, keeping the position of the first occurrence. As
   * custom mappings are consulted before the default ones, a mapping for a broad type (e.g. {@code
   * Number.class} or {@code Object.class}) hides default mappings of its subtypes.
   *
   * <p>Example:
   *
   * <pre>{@code
   * @Bean
   * TypeNameMapper typeNameMapper() {
   *   return new SimpleTypeNameMapper(
   *       new TypeNameEntry(UUID.class, "uuid"),
   *       new TypeNameEntry(LocalDate.class, "date"),
   *       new TypeNameEntry(Temporal.class, "date-time"));
   * }
   * }</pre>
   *
   * @param entries types and names they are mapped to, in order of comparison
   * @throws NullPointerException if {@code entries} or any of its elements is {@code null}
   * @since 3.1.0
   */
  public SimpleTypeNameMapper(TypeNameEntry... entries) {
    this(Arrays.asList(requireNonNull(entries, "entries must not be null")));
  }

  /**
   * Creates a new {@link SimpleTypeNameMapper} with custom mappings, taking precedence over the
   * {@linkplain SimpleTypeNameMapper default ones}. Custom mappings are matched the same way as in
   * {@link #SimpleTypeNameMapper(TypeNameEntry...)}.
   *
   * @param entries types and names they are mapped to, in order of comparison, copied on creation
   * @throws NullPointerException if {@code entries} or any of its elements is {@code null}
   * @since 3.1.0
   */
  public SimpleTypeNameMapper(List<TypeNameEntry> entries) {
    requireNonNull(entries, "entries must not be null");
    for (TypeNameEntry entry : entries) {
      requireNonNull(entry, "entries must not contain null elements");
      mappings.put(entry.getType(), entry.getName());
    }
  }

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
   * Maps a Java type to a common type name. Custom mappings passed to {@link
   * #SimpleTypeNameMapper(TypeNameEntry...)} are consulted first, then the {@linkplain
   * SimpleTypeNameMapper default mappings}.
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
    Optional<String> custom = findMapping(type);
    if (custom.isPresent()) {
      return custom;
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

  private Optional<String> findMapping(Class<?> type) {
    Class<?> boxed = type.isPrimitive() ? MethodType.methodType(type).wrap().returnType() : type;
    for (Map.Entry<Class<?>, String> mapping : mappings.entrySet()) {
      Class<?> mappedType = mapping.getKey();
      if (mappedType == type || mappedType.isAssignableFrom(boxed)) {
        return Optional.of(mapping.getValue());
      }
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
