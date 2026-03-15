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
package io.github.problem4j.spring.web.resolver;
