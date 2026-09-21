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

import io.github.problem4j.spring.web.ProblemFormat;
import io.github.problem4j.spring.web.ProblemFormatAware;
import io.github.problem4j.spring.web.ProblemSupportAware;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupportAware;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupportAware;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Single {@link BeanPostProcessor} that injects the container's Problem4J collaborators into every
 * bean that opts in through one of the {@code *Aware} callback interfaces, right after
 * construction.
 *
 * <p>This lets each {@code ProblemResolver} be created with its default (no-arg) constructor - the
 * dependencies it needs are pushed in afterward instead of being threaded through constructors. The
 * following callbacks are handled:
 *
 * <ul>
 *   <li>{@link ProblemFormatAware} - configured with the {@link ProblemFormat} bean
 *   <li>{@link TypeNameMapperAware} - configured with the {@link TypeNameMapper} bean
 *   <li>{@link BindingResultSupportAware} - configured with the {@link BindingResultSupport} bean
 *   <li>{@link MethodValidationResultSupportAware} - configured with the {@link
 *       MethodValidationResultSupport} bean
 *   <li>{@link MethodParameterSupportAware} - configured with the {@link MethodParameterSupport}
 *       bean
 * </ul>
 *
 * <p>Every collaborator is taken via a {@link Supplier} (resolved lazily, on first use, e.g. {@code
 * ObjectProvider::getIfAvailable}) rather than direct constructor injection, so that registering
 * this processor as a bean does not force early instantiation of the collaborator beans. A supplier
 * is only queried when a bean actually implements the matching callback interface, and a {@code
 * null} result leaves the bean untouched.
 *
 * <p>Beans that implement none of the callbacks are skipped with a single {@link
 * ProblemSupportAware} {@code instanceof} check, since every callback extends it.
 *
 * @since 3.1.0
 */
public interface ProblemBeanPostProcessor extends BeanPostProcessor {

  /**
   * Creates a new {@link Builder} for {@link ProblemBeanPostProcessor}. Every collaborator supplier
   * defaults to one returning {@code null}, so a bean implementing the matching callback is left
   * untouched unless the supplier is set.
   *
   * @return a new {@link Builder}
   * @since 3.1.0
   */
  static Builder builder() {
    return new DefaultProblemBeanPostProcessor.DefaultBuilder();
  }

  /**
   * Apply this {@link ProblemBeanPostProcessor} to the given new bean instance.
   *
   * @param bean the new bean instance
   * @param beanName the name of the bean
   * @return the bean instance to use, either the original or a modified one
   * @since 3.1.0
   */
  @Override
  Object postProcessBeforeInitialization(Object bean, String beanName);

  /**
   * Builder for {@link ProblemBeanPostProcessor}. Every collaborator supplier not set explicitly
   * returns {@code null}.
   *
   * @since 3.1.0
   */
  interface Builder {

    /**
     * Sets the supplier of the {@link ProblemFormat} bean.
     *
     * @param problemFormat supplier of the {@link ProblemFormat} bean
     * @return this builder
     * @throws NullPointerException if {@code problemFormat} is {@code null}
     * @since 3.1.0
     */
    Builder problemFormat(Supplier<@Nullable ProblemFormat> problemFormat);

    /**
     * Sets the supplier of the {@link TypeNameMapper} bean.
     *
     * @param typeNameMapper supplier of the {@link TypeNameMapper} bean
     * @return this builder
     * @throws NullPointerException if {@code typeNameMapper} is {@code null}
     * @since 3.1.0
     */
    Builder typeNameMapper(Supplier<@Nullable TypeNameMapper> typeNameMapper);

    /**
     * Sets the supplier of the {@link BindingResultSupport} bean.
     *
     * @param bindingResultSupport supplier of the {@link BindingResultSupport} bean
     * @return this builder
     * @throws NullPointerException if {@code bindingResultSupport} is {@code null}
     * @since 3.1.0
     */
    Builder bindingResultSupport(Supplier<@Nullable BindingResultSupport> bindingResultSupport);

    /**
     * Sets the supplier of the {@link MethodValidationResultSupport} bean.
     *
     * @param methodValidationResultSupport supplier of the {@link MethodValidationResultSupport}
     *     bean
     * @return this builder
     * @throws NullPointerException if {@code methodValidationResultSupport} is {@code null}
     * @since 3.1.0
     */
    Builder methodValidationResultSupport(
        Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport);

    /**
     * Sets the supplier of the {@link MethodParameterSupport} bean.
     *
     * @param methodParameterSupport supplier of the {@link MethodParameterSupport} bean
     * @return this builder
     * @throws NullPointerException if {@code methodParameterSupport} is {@code null}
     * @since 3.1.0
     */
    Builder methodParameterSupport(
        Supplier<@Nullable MethodParameterSupport> methodParameterSupport);

    /**
     * Builds a new {@link ProblemBeanPostProcessor} from the configured suppliers.
     *
     * @return a new {@link ProblemBeanPostProcessor}
     * @since 3.1.0
     */
    ProblemBeanPostProcessor build();
  }
}
