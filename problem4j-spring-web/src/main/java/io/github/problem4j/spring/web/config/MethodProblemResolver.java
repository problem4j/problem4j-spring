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

package io.github.problem4j.spring.web.config;

import io.github.problem4j.core.Problem;
import io.github.problem4j.core.ProblemContext;
import io.github.problem4j.spring.web.ProblemHandler;
import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.ReflectionUtils;

/** {@link ProblemResolver} adapter invoking a method annotated with {@link ProblemHandler}. */
final class MethodProblemResolver implements ProblemResolver, Ordered {

  private final Class<? extends Exception> exceptionClass;
  private final Supplier<Object> target;
  private final Method method;
  private final int order;
  private volatile @Nullable Method invocableMethod;

  MethodProblemResolver(
      Class<? extends Exception> exceptionClass,
      Supplier<Object> target,
      Method method,
      int order) {
    this.exceptionClass = exceptionClass;
    this.target = target;
    this.method = method;
    this.order = order;
  }

  @Override
  public Class<? extends Exception> getExceptionClass() {
    return exceptionClass;
  }

  @Override
  public int getOrder() {
    return order;
  }

  @Override
  public Problem resolve(
      ProblemContext context, Exception ex, HttpHeaders headers, HttpStatusCode status) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    @Nullable Object[] args = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      args[i] = resolveArgument(parameterTypes[i], context, ex, headers, status);
    }

    Object bean = target.get();
    Object result = ReflectionUtils.invokeMethod(invocableMethod(bean), bean, args);
    if (result == null) {
      throw new IllegalStateException(
          "@ProblemHandler method " + method.toGenericString() + " returned null");
    }
    return (Problem) result;
  }

  // resolved against the actual bean, as it may be a JDK proxy exposing only interface methods
  private Method invocableMethod(Object bean) {
    Method invocable = invocableMethod;
    if (invocable == null) {
      invocable = AopUtils.selectInvocableMethod(method, bean.getClass());
      ReflectionUtils.makeAccessible(invocable);
      invocableMethod = invocable;
    }
    return invocable;
  }

  private static Object resolveArgument(
      Class<?> parameterType,
      ProblemContext context,
      Exception ex,
      HttpHeaders headers,
      HttpStatusCode status) {
    if (parameterType == ProblemContext.class) {
      return context;
    }
    if (parameterType == HttpHeaders.class) {
      return headers;
    }
    if (parameterType == HttpStatusCode.class) {
      return status;
    }
    return ex;
  }

  @Override
  public String toString() {
    return "MethodProblemResolver[" + exceptionClass.getName() + " -> " + method + "]";
  }
}
