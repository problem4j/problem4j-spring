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

import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Single {@link BeanPostProcessor} that injects the container's Problem4J collaborators into every
 * bean that opts in through one of the {@code *Aware} callback interfaces, right after
 * construction.
 *
 * <p>This lets each {@code ProblemResolver} be created with its default (no-arg) constructor - the
 * dependencies it needs are pushed in afterwards instead of being threaded through constructors.
 *
 * @since 3.1.0
 */
public interface ProblemBeanPostProcessor extends BeanPostProcessor {

  /**
   * Invokes every {@code *Aware} callback the given bean implements with the corresponding
   * container bean.
   *
   * @param bean the newly constructed bean
   * @param beanName the name of the bean
   * @return {@code bean}, unchanged
   * @throws BeansException never thrown by this implementation
   * @since 3.1.0
   */
  @Override
  @Nullable Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException;
}
