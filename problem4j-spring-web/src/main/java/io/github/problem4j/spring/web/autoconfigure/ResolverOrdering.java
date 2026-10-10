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

package io.github.problem4j.spring.web.autoconfigure;

import io.github.problem4j.spring.web.resolver.ProblemResolver;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

/**
 * Creates comparators deciding {@link ProblemResolver} precedence the same way Spring orders
 * injected collections, i.e. also considering {@link org.springframework.core.annotation.Order
 * &#64;Order} declared on {@code @Bean} factory methods.
 */
final class ResolverOrdering {

  /**
   * Creates a comparator for the given resolver beans.
   *
   * @param beanFactory bean factory holding the resolver bean definitions
   * @param problemResolvers resolvers by bean name
   * @return comparator ordering resolvers by precedence (lower wins)
   */
  static Comparator<Object> comparator(
      ConfigurableListableBeanFactory beanFactory,
      Map<String, ? extends ProblemResolver> problemResolvers) {
    IdentityHashMap<Object, Object> sources = new IdentityHashMap<>();
    problemResolvers.forEach(
        (beanName, resolver) -> {
          Object source = findOrderSource(beanFactory, beanName, resolver);
          if (source != null) {
            sources.put(resolver, source);
          }
        });
    return AnnotationAwareOrderComparator.INSTANCE.withSourceProvider(sources::get);
  }

  private static @Nullable Object findOrderSource(
      ConfigurableListableBeanFactory beanFactory, String beanName, ProblemResolver resolver) {
    if (!beanFactory.containsBeanDefinition(beanName)) {
      return null;
    }
    BeanDefinition definition = beanFactory.getMergedBeanDefinition(beanName);
    if (!(definition instanceof RootBeanDefinition rootDefinition)) {
      return null;
    }

    List<Object> sources = new ArrayList<>(2);
    Method factoryMethod = rootDefinition.getResolvedFactoryMethod();
    if (factoryMethod != null) {
      sources.add(factoryMethod);
    }
    Class<?> targetType = rootDefinition.getTargetType();
    if (targetType != null && targetType != resolver.getClass()) {
      sources.add(targetType);
    }
    return sources.isEmpty() ? null : sources.toArray();
  }

  private ResolverOrdering() {}
}
