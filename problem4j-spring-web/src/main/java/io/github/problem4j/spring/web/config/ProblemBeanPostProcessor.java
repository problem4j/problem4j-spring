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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Single {@link BeanPostProcessor} that injects the container's Problem4J collaborators into every
 * bean that opts in through one of the {@code *Aware} callback interfaces, right after
 * construction.
 *
 * <p>This lets each {@code ProblemResolver} be created with its default (no-arg) constructor - the
 * dependencies it needs are pushed in afterwards instead of being threaded through constructors.
 * The following callbacks are handled:
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
 * <p>Every collaborator is taken via an {@link ObjectProvider} (resolved lazily, on first use)
 * rather than direct constructor injection, so that registering this processor as a bean does not
 * force early instantiation of the collaborator beans. A provider is only queried when a bean
 * actually implements the matching callback interface.
 *
 * @since 3.1.0
 */
public class ProblemBeanPostProcessor implements BeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(ProblemBeanPostProcessor.class);

  private final ObjectProvider<ProblemFormat> problemFormat;
  private final ObjectProvider<TypeNameMapper> typeNameMapper;
  private final ObjectProvider<BindingResultSupport> bindingResultSupport;
  private final ObjectProvider<MethodValidationResultSupport> methodValidationResultSupport;
  private final ObjectProvider<MethodParameterSupport> methodParameterSupport;

  /**
   * Creates a new {@link ProblemBeanPostProcessor}.
   *
   * @param problemFormat provider for the container's {@link ProblemFormat} bean
   * @param typeNameMapper provider for the container's {@link TypeNameMapper} bean
   * @param bindingResultSupport provider for the container's {@link BindingResultSupport} bean
   * @param methodValidationResultSupport provider for the container's {@link
   *     MethodValidationResultSupport} bean
   * @param methodParameterSupport provider for the container's {@link MethodParameterSupport} bean
   * @since 3.1.0
   */
  public ProblemBeanPostProcessor(
      ObjectProvider<ProblemFormat> problemFormat,
      ObjectProvider<TypeNameMapper> typeNameMapper,
      ObjectProvider<BindingResultSupport> bindingResultSupport,
      ObjectProvider<MethodValidationResultSupport> methodValidationResultSupport,
      ObjectProvider<MethodParameterSupport> methodParameterSupport) {
    this.problemFormat = problemFormat;
    this.typeNameMapper = typeNameMapper;
    this.bindingResultSupport = bindingResultSupport;
    this.methodValidationResultSupport = methodValidationResultSupport;
    this.methodParameterSupport = methodParameterSupport;
  }

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
  public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName)
      throws BeansException {
    List<String> auditLog = new ArrayList<>(5);
    maybeAddProblemFormatAware(bean, auditLog);
    maybeAddTypeNameMapper(bean, auditLog);
    maybeAddBindingResultSupport(bean, auditLog);
    maybeAddMethodValidationResultSupport(bean, auditLog);
    maybeAddMethodParameterSupport(bean, auditLog);

    if (log.isDebugEnabled() && !auditLog.isEmpty()) {
      log.debug("Enhanced {} bean with {}", beanName, asLogLine(auditLog));
    }
    return bean;
  }

  private void maybeAddProblemFormatAware(Object bean, List<String> auditLog) {
    if (bean instanceof ProblemFormatAware aware) {
      Optional.ofNullable(problemFormat.getIfAvailable())
          .ifPresent(
              object -> {
                register(auditLog, object);
                aware.setProblemFormat(object);
              });
    }
  }

  private void maybeAddTypeNameMapper(Object bean, List<String> auditLog) {
    if (bean instanceof TypeNameMapperAware aware) {
      Optional.ofNullable(typeNameMapper.getIfAvailable())
          .ifPresent(
              object -> {
                register(auditLog, object);
                aware.setTypeNameMapper(object);
              });
    }
  }

  private void maybeAddBindingResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof BindingResultSupportAware aware) {
      Optional.ofNullable(bindingResultSupport.getIfAvailable())
          .ifPresent(
              object -> {
                register(auditLog, object);
                aware.setBindingResultSupport(object);
              });
    }
  }

  private void maybeAddMethodValidationResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodValidationResultSupportAware aware) {
      Optional.ofNullable(methodValidationResultSupport.getIfAvailable())
          .ifPresent(
              object -> {
                register(auditLog, object);
                aware.setMethodValidationResultSupport(object);
              });
    }
  }

  private void maybeAddMethodParameterSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodParameterSupportAware aware) {
      Optional.ofNullable(methodParameterSupport.getIfAvailable())
          .ifPresent(
              object -> {
                register(auditLog, object);
                aware.setMethodParameterSupport(object);
              });
    }
  }

  private void register(List<String> auditLog, Object bean) {
    if (log.isDebugEnabled()) {
      auditLog.add(AopUtils.getTargetClass(bean).getSimpleName());
    }
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
}
