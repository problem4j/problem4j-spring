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

import io.github.problem4j.spring.web.ProblemSupportAware;

/**
 * Callback interface for components that want to be configured with a {@link
 * MethodParameterSupport} after construction, instead of requiring it as a constructor argument.
 *
 * <p>When {@code problem4j-spring-web} autoconfiguration is active, any bean implementing this
 * interface is detected by {@link io.github.problem4j.spring.web.config.ProblemBeanPostProcessor
 * ProblemBeanPostProcessor} and configured with the container's {@link MethodParameterSupport}
 * bean.
 *
 * @see MethodParameterSupport
 * @since 3.1.0
 */
public interface MethodParameterSupportAware extends ProblemSupportAware {

  /**
   * Replaces the {@link MethodParameterSupport} used by this resolver.
   *
   * @param methodParameterSupport the method parameter support to use
   * @since 3.1.0
   */
  void setMethodParameterSupport(MethodParameterSupport methodParameterSupport);
}
