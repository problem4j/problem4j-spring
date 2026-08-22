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

package io.github.problem4j.spring.web.parameter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Injects the container's {@link BindingResultSupport} bean into every {@link
 * BindingResultSupportAware} bean, right after construction.
 *
 * @see BindingResultSupportAware
 * @since 3.1.0
 */
public final class BindingResultSupportAwareBeanPostProcessor implements BeanPostProcessor {

  private final ObjectProvider<BindingResultSupport> bindingResultSupport;

  /**
   * Creates a new {@link BindingResultSupportAwareBeanPostProcessor}.
   *
   * @param bindingResultSupport provider for the container's {@link BindingResultSupport} bean
   * @since 3.1.0
   */
  public BindingResultSupportAwareBeanPostProcessor(
      ObjectProvider<BindingResultSupport> bindingResultSupport) {
    this.bindingResultSupport = bindingResultSupport;
  }

  /**
   * Calls {@link BindingResultSupportAware#setBindingResultSupport(BindingResultSupport)} if {@code
   * bean} implements {@link BindingResultSupportAware}.
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
    if (bean instanceof BindingResultSupportAware aware) {
      aware.setBindingResultSupport(bindingResultSupport.getObject());
    }
    return bean;
  }
}
