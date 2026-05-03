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

/**
 * Exception-to-problem resolution framework.
 *
 * <p>Provides the {@code ProblemResolver} interface and a comprehensive set of built-in
 * implementations for mapping Spring framework exceptions and validation errors to RFC 7807 {@code
 * Problem} responses. Resolvers handle:
 *
 * <ul>
 *   <li>Validation errors (constraint violations, binding errors, method validation)
 *   <li>Request errors (HTTP method not supported, media type issues, missing values)
 *   <li>Parsing errors (HTTP message not readable, decoding problems)
 *   <li>Server errors (error response mapping, exception handling)
 * </ul>
 *
 * <p>Custom resolvers can be registered with {@code ProblemResolverStore} to extend the framework's
 * default behavior.
 */
@NullMarked
package io.github.problem4j.spring.web.resolver;

import org.jspecify.annotations.NullMarked;
