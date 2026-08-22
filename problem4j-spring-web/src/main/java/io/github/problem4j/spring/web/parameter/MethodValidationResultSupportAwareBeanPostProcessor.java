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
 * Injects the container's {@link MethodValidationResultSupport} bean into every {@link
 * MethodValidationResultSupportAware} bean, right after construction.
 *
 * @see MethodValidationResultSupportAware
 * @since 3.1.0
 */
public final class MethodValidationResultSupportAwareBeanPostProcessor
    implements BeanPostProcessor {

  private final ObjectProvider<MethodValidationResultSupport> methodValidationResultSupport;

  /**
   * Creates a new {@link MethodValidationResultSupportAwareBeanPostProcessor}.
   *
   * @param methodValidationResultSupport provider for the container's {@link
   *     MethodValidationResultSupport} bean
   * @since 3.1.0
   */
  public MethodValidationResultSupportAwareBeanPostProcessor(
      ObjectProvider<MethodValidationResultSupport> methodValidationResultSupport) {
    this.methodValidationResultSupport = methodValidationResultSupport;
  }

  /**
   * Calls {@link
   * MethodValidationResultSupportAware#setMethodValidationResultSupport(MethodValidationResultSupport)}
   * if {@code bean} implements {@link MethodValidationResultSupportAware}.
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
    if (bean instanceof MethodValidationResultSupportAware aware) {
      aware.setMethodValidationResultSupport(methodValidationResultSupport.getObject());
    }
    return bean;
  }
}
