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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.ClassUtils;
import org.springframework.util.function.SingletonSupplier;

/**
 * {@link BeanFactoryPostProcessor} registering a {@link ProblemResolver} bean for each method
 * annotated with {@link ProblemHandler} (one per resolved exception class).
 *
 * <p>Registered resolvers are regular beans, so they are picked up by {@code ProblemResolverStore}
 * like any other {@link ProblemResolver}. Their precedence is taken from {@link Order @Order} on
 * the annotated method ({@link Ordered#LOWEST_PRECEDENCE} if absent).
 *
 * <p>Private methods are ignored. Invalid method signatures fail application startup.
 *
 * @since 3.1.0
 */
public final class ProblemHandlerMethodProcessor implements BeanFactoryPostProcessor {

  /**
   * Creates a new {@link ProblemHandlerMethodProcessor}.
   *
   * @since 3.1.0
   */
  public ProblemHandlerMethodProcessor() {}

  /**
   * Scans bean definitions for methods annotated with {@link ProblemHandler} and registers a {@link
   * ProblemResolver} bean definition for each of them.
   *
   * @param beanFactory the bean factory
   * @since 3.1.0
   */
  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
      return;
    }

    for (String beanName : beanFactory.getBeanDefinitionNames()) {
      if (ScopedProxyUtils.isScopedTarget(beanName)
          || beanFactory.getBeanDefinition(beanName).isAbstract()) {
        continue;
      }

      Class<?> beanType = beanFactory.getType(beanName, false);
      if (beanType == null || !AnnotationUtils.isCandidateClass(beanType, ProblemHandler.class)) {
        continue;
      }

      Map<Method, ProblemHandler> methods =
          MethodIntrospector.selectMethods(
              ClassUtils.getUserClass(beanType),
              (MethodIntrospector.MetadataLookup<ProblemHandler>)
                  method ->
                      Modifier.isPrivate(method.getModifiers())
                          ? null
                          : AnnotatedElementUtils.findMergedAnnotation(
                              method, ProblemHandler.class));

      methods.forEach(
          (method, mapping) -> register(registry, beanFactory, beanName, method, mapping));
    }
  }

  private static void register(
      BeanDefinitionRegistry registry,
      ConfigurableListableBeanFactory beanFactory,
      String beanName,
      Method method,
      ProblemHandler mapping) {
    Supplier<Object> target = SingletonSupplier.of(() -> beanFactory.getBean(beanName))::obtain;
    Order order = AnnotatedElementUtils.findMergedAnnotation(method, Order.class);
    int orderValue = order != null ? order.value() : Ordered.LOWEST_PRECEDENCE;

    for (Class<? extends Exception> exceptionClass : resolveExceptionClasses(method, mapping)) {
      RootBeanDefinition definition =
          new RootBeanDefinition(
              MethodProblemResolver.class,
              () -> new MethodProblemResolver(exceptionClass, target, method, orderValue));
      registry.registerBeanDefinition(
          beanName + "#" + method.getName() + "#" + exceptionClass.getName(), definition);
    }
  }

  private static List<Class<? extends Exception>> resolveExceptionClasses(
      Method method, ProblemHandler mapping) {
    if (!Problem.class.isAssignableFrom(method.getReturnType())) {
      throw invalid(method, "must return " + Problem.class.getName());
    }

    Class<?> exceptionParameter = findExceptionParameter(method);

    if (mapping.value().length == 0) {
      if (exceptionParameter == null) {
        throw invalid(
            method, "must declare an Exception parameter or list exception classes in value");
      }
      return List.of(exceptionParameter.asSubclass(Exception.class));
    }

    List<Class<? extends Exception>> exceptionClasses = new ArrayList<>();
    for (Class<? extends Exception> exceptionClass : mapping.value()) {
      if (exceptionParameter != null && !exceptionParameter.isAssignableFrom(exceptionClass)) {
        throw invalid(
            method,
            exceptionClass.getName()
                + " is not assignable to parameter "
                + exceptionParameter.getName());
      }
      exceptionClasses.add(exceptionClass);
    }
    return exceptionClasses;
  }

  private static @Nullable Class<?> findExceptionParameter(Method method) {
    Class<?> exceptionParameter = null;
    for (Class<?> parameterType : method.getParameterTypes()) {
      if (parameterType == ProblemContext.class
          || parameterType == HttpHeaders.class
          || parameterType == HttpStatusCode.class) {
        continue;
      }
      if (!Exception.class.isAssignableFrom(parameterType)) {
        throw invalid(method, "has unsupported parameter type " + parameterType.getName());
      }
      if (exceptionParameter != null) {
        throw invalid(method, "must declare at most one exception parameter");
      }
      exceptionParameter = parameterType;
    }
    return exceptionParameter;
  }

  private static IllegalStateException invalid(Method method, String reason) {
    return new IllegalStateException(
        "@ProblemHandler method " + method.toGenericString() + " " + reason);
  }
}
