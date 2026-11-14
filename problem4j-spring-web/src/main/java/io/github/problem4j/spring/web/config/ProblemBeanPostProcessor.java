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
import io.github.problem4j.spring.web.ProblemUtilityAware;
import io.github.problem4j.spring.web.TypeNameMapper;
import io.github.problem4j.spring.web.TypeNameMapperAware;
import io.github.problem4j.spring.web.parameter.BindingResultSupport;
import io.github.problem4j.spring.web.parameter.BindingResultSupportAware;
import io.github.problem4j.spring.web.parameter.MethodParameterSupport;
import io.github.problem4j.spring.web.parameter.MethodParameterSupportAware;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupport;
import io.github.problem4j.spring.web.parameter.MethodValidationResultSupportAware;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
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
 * <p>Built-in resolvers keep collaborators passed explicitly to their (deprecated) constructors,
 * ignoring the values offered by this processor, for backwards compatibility with 3.0.0.
 *
 * <p>Beans that implement none of the callbacks are skipped with a single {@link
 * ProblemUtilityAware} {@code instanceof} check, since every callback extends it.
 *
 * @since 3.1.0
 */
public class ProblemBeanPostProcessor implements BeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(ProblemBeanPostProcessor.class);

  /**
   * Creates a new {@link Builder} for {@link ProblemBeanPostProcessor}. Every collaborator supplier
   * defaults to one returning {@code null}, so a bean implementing the matching callback is left
   * untouched unless the supplier is set.
   *
   * @return a new {@link Builder}
   * @since 3.1.0
   */
  public static Builder builder() {
    return new Builder();
  }

  private final Supplier<@Nullable ProblemFormat> problemFormat;
  private final Supplier<@Nullable TypeNameMapper> typeNameMapper;
  private final Supplier<@Nullable BindingResultSupport> bindingResultSupport;
  private final Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport;
  private final Supplier<@Nullable MethodParameterSupport> methodParameterSupport;

  /**
   * Creates a new {@link ProblemBeanPostProcessor} from the suppliers configured in the given
   * {@link Builder}. Intended for subclasses; otherwise use {@link #builder()}.
   *
   * @param builder builder holding the collaborator suppliers
   * @since 3.1.0
   */
  protected ProblemBeanPostProcessor(Builder builder) {
    this.problemFormat = builder.problemFormat;
    this.typeNameMapper = builder.typeNameMapper;
    this.bindingResultSupport = builder.bindingResultSupport;
    this.methodValidationResultSupport = builder.methodValidationResultSupport;
    this.methodParameterSupport = builder.methodParameterSupport;
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
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    if (!(bean instanceof ProblemUtilityAware)) {
      return bean;
    }

    List<String> auditLog = new ArrayList<>(5);
    maybeAddProblemFormatAware(bean, auditLog);
    maybeAddTypeNameMapper(bean, auditLog);
    maybeAddBindingResultSupport(bean, auditLog);
    maybeAddMethodValidationResultSupport(bean, auditLog);
    maybeAddMethodParameterSupport(bean, auditLog);

    if (log.isDebugEnabled() && !auditLog.isEmpty()) {
      log.debug(
          "Enhanced '{}' [{}] bean with {}",
          beanName,
          AopUtils.getTargetClass(bean).getName(),
          asLogLine(auditLog));
    }
    return bean;
  }

  private void maybeAddProblemFormatAware(Object bean, List<String> auditLog) {
    if (bean instanceof ProblemFormatAware aware) {
      Optional.ofNullable(problemFormat.get())
          .ifPresent(object -> {
            saveAuditLog(auditLog, object);
            aware.setProblemFormat(object);
          });
    }
  }

  private void maybeAddTypeNameMapper(Object bean, List<String> auditLog) {
    if (bean instanceof TypeNameMapperAware aware) {
      Optional.ofNullable(typeNameMapper.get())
          .ifPresent(object -> {
            saveAuditLog(auditLog, object);
            aware.setTypeNameMapper(object);
          });
    }
  }

  private void maybeAddBindingResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof BindingResultSupportAware aware) {
      Optional.ofNullable(bindingResultSupport.get())
          .ifPresent(object -> {
            saveAuditLog(auditLog, object);
            aware.setBindingResultSupport(object);
          });
    }
  }

  private void maybeAddMethodValidationResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodValidationResultSupportAware aware) {
      Optional.ofNullable(methodValidationResultSupport.get())
          .ifPresent(object -> {
            saveAuditLog(auditLog, object);
            aware.setMethodValidationResultSupport(object);
          });
    }
  }

  private void maybeAddMethodParameterSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodParameterSupportAware aware) {
      Optional.ofNullable(methodParameterSupport.get())
          .ifPresent(object -> {
            saveAuditLog(auditLog, object);
            aware.setMethodParameterSupport(object);
          });
    }
  }

  private void saveAuditLog(List<String> auditLog, Object bean) {
    auditLog.add(AopUtils.getTargetClass(bean).getSimpleName());
  }

  private String asLogLine(List<String> auditLog) {
    return switch (auditLog.size()) {
      case 1 -> auditLog.get(0);
      case 2 -> String.join(" and ", auditLog);
      default ->
          String.join(", ", auditLog.subList(0, auditLog.size() - 1))
              + " and "
              + auditLog.get(auditLog.size() - 1);
    };
  }

  /**
   * Builder for {@link ProblemBeanPostProcessor}. Every collaborator supplier not set explicitly
   * returns {@code null}.
   *
   * @since 3.1.0
   */
  public static final class Builder {

    private Supplier<@Nullable ProblemFormat> problemFormat = () -> null;
    private Supplier<@Nullable TypeNameMapper> typeNameMapper = () -> null;
    private Supplier<@Nullable BindingResultSupport> bindingResultSupport = () -> null;
    private Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport =
        () -> null;
    private Supplier<@Nullable MethodParameterSupport> methodParameterSupport = () -> null;

    private Builder() {}

    /**
     * Sets the supplier of the {@link ProblemFormat} bean.
     *
     * @param problemFormat supplier of the {@link ProblemFormat} bean
     * @return this builder
     * @since 3.1.0
     */
    public Builder problemFormat(Supplier<@Nullable ProblemFormat> problemFormat) {
      this.problemFormat = problemFormat;
      return this;
    }

    /**
     * Sets the supplier of the {@link TypeNameMapper} bean.
     *
     * @param typeNameMapper supplier of the {@link TypeNameMapper} bean
     * @return this builder
     * @since 3.1.0
     */
    public Builder typeNameMapper(Supplier<@Nullable TypeNameMapper> typeNameMapper) {
      this.typeNameMapper = typeNameMapper;
      return this;
    }

    /**
     * Sets the supplier of the {@link BindingResultSupport} bean.
     *
     * @param bindingResultSupport supplier of the {@link BindingResultSupport} bean
     * @return this builder
     * @since 3.1.0
     */
    public Builder bindingResultSupport(
        Supplier<@Nullable BindingResultSupport> bindingResultSupport) {
      this.bindingResultSupport = bindingResultSupport;
      return this;
    }

    /**
     * Sets the supplier of the {@link MethodValidationResultSupport} bean.
     *
     * @param methodValidationResultSupport supplier of the {@link MethodValidationResultSupport}
     *     bean
     * @return this builder
     * @since 3.1.0
     */
    public Builder methodValidationResultSupport(
        Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport) {
      this.methodValidationResultSupport = methodValidationResultSupport;
      return this;
    }

    /**
     * Sets the supplier of the {@link MethodParameterSupport} bean.
     *
     * @param methodParameterSupport supplier of the {@link MethodParameterSupport} bean
     * @return this builder
     * @since 3.1.0
     */
    public Builder methodParameterSupport(
        Supplier<@Nullable MethodParameterSupport> methodParameterSupport) {
      this.methodParameterSupport = methodParameterSupport;
      return this;
    }

    /**
     * Builds a new {@link ProblemBeanPostProcessor} from the configured suppliers.
     *
     * @return a new {@link ProblemBeanPostProcessor}
     * @since 3.1.0
     */
    public ProblemBeanPostProcessor build() {
      return new ProblemBeanPostProcessor(this);
    }
  }
}
