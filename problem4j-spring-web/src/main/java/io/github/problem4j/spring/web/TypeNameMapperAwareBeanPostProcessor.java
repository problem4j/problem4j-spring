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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Injects the container's {@link TypeNameMapper} bean into every {@link TypeNameMapperAware} bean,
 * right after construction.
 *
 * <p>Depends on {@link TypeNameMapper} via {@link ObjectProvider} (resolved lazily, on first use)
 * rather than direct constructor injection, so that registering this processor as a bean does not
 * force early instantiation of the {@link TypeNameMapper} bean.
 *
 * @see TypeNameMapperAware
 * @since 3.1.0
 */
public final class TypeNameMapperAwareBeanPostProcessor implements BeanPostProcessor {

  private final ObjectProvider<TypeNameMapper> typeNameMapper;

  /**
   * Creates a new {@link TypeNameMapperAwareBeanPostProcessor}.
   *
   * @param typeNameMapper provider for the container's {@link TypeNameMapper} bean
   * @since 3.1.0
   */
  public TypeNameMapperAwareBeanPostProcessor(ObjectProvider<TypeNameMapper> typeNameMapper) {
    this.typeNameMapper = typeNameMapper;
  }

  /**
   * Calls {@link TypeNameMapperAware#setTypeNameMapper(TypeNameMapper)} if {@code bean} implements
   * {@link TypeNameMapperAware}.
   *
   * @param bean the newly constructed bean
   * @param beanName the name of the bean
   * @return {@code bean}, unchanged
   * @throws BeansException never thrown by this implementation
   * @since 3.1.0
   */
  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    if (bean instanceof TypeNameMapperAware aware) {
      aware.setTypeNameMapper(typeNameMapper.getObject());
    }
    return bean;
  }
}
