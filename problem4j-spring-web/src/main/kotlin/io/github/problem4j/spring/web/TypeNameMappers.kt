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

package io.github.problem4j.spring.web

import kotlin.reflect.KClass

/**
 * Creates a [SimpleTypeNameMapper] with custom mappings from `type to name` pairs, taking
 * precedence over its default mappings (see [SimpleTypeNameMapper]). Pairs are compared in given
 * order and the first one matching wins. A pair matches its own type and all of its subtypes, so
 * pairs for more specific types should be given before pairs for their supertypes. For types with a
 * primitive counterpart, such as [Int], both the primitive and the boxed Java type are matched. If
 * the same type is given more than once, the last name wins, keeping the position of the first
 * occurrence. As pairs are consulted before the default mappings, a pair for a broad type (e.g.
 * [Number] or [Any]) hides default mappings of its subtypes.
 *
 * Example:
 * ```
 * import io.github.problem4j.spring.web.TypeNameMapper
 * import io.github.problem4j.spring.web.simpleTypeNameMapper
 * import java.time.LocalDate
 * import java.time.temporal.Temporal
 * import java.util.UUID
 * import org.springframework.context.annotation.Bean
 *
 * @Bean
 * fun typeNameMapper(): TypeNameMapper =
 *     simpleTypeNameMapper(
 *         UUID::class to "uuid",
 *         LocalDate::class to "date",
 *         Temporal::class to "date-time",
 *     )
 * ```
 *
 * @param mappings pairs of types and names they are mapped to, in order of comparison
 * @return a new [SimpleTypeNameMapper] with given custom mappings
 * @since 3.1.0
 */
public fun simpleTypeNameMapper(vararg mappings: Pair<KClass<*>, String>): SimpleTypeNameMapper =
    SimpleTypeNameMapper(mappings.map { (type, name) -> TypeNameEntry(type.javaObjectType, name) })
