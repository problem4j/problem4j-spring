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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a bean method as a {@link io.github.problem4j.spring.web.resolver.ProblemResolver
 * ProblemResolver}, so it is registered alongside other resolvers without implementing the
 * interface.
 *
 * <p>The method must return {@code Problem} and may declare any of the following parameters, in any
 * order:
 *
 * <ul>
 *   <li>the resolved exception (at most one parameter of an {@link Exception} type),
 *   <li>{@code ProblemContext},
 *   <li>{@code HttpHeaders},
 *   <li>{@code HttpStatusCode}.
 * </ul>
 *
 * <pre>{@code
 * @Component
 * class OrderProblemHandlers {
 *
 *   @ProblemHandler
 *   Problem outOfStock(OutOfStockException ex, ProblemContext context) {
 *     return Problem.builder()
 *         .status(409)
 *         .extension("sku", ex.getSku())
 *         .build();
 *   }
 * }
 * }</pre>
 *
 * <p>Precedence among resolvers for the same exception class can be set with {@link
 * org.springframework.core.annotation.Order @Order} on the method (lower value wins).
 *
 * <p>Private methods are ignored. Methods are discovered on bean types known before instantiation,
 * so for {@code @Bean} factory methods the declared return type must expose the annotated methods.
 *
 * @see io.github.problem4j.spring.web.config.ProblemHandlerMethodProcessor
 * @since 3.1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProblemHandler {

  /**
   * Exception classes resolved by the annotated method. If empty, the type of the exception
   * parameter is used. Each class must be assignable to the exception parameter, if declared.
   *
   * @return resolved exception classes
   * @since 3.1.0
   */
  Class<? extends Exception>[] value() default {};
}
