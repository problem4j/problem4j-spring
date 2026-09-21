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

import static java.util.Objects.requireNonNull;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

final class DefaultProblemBeanPostProcessor implements ProblemBeanPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(DefaultProblemBeanPostProcessor.class);

  private final Supplier<@Nullable ProblemFormat> problemFormat;
  private final Supplier<@Nullable TypeNameMapper> typeNameMapper;
  private final Supplier<@Nullable BindingResultSupport> bindingResultSupport;
  private final Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport;
  private final Supplier<@Nullable MethodParameterSupport> methodParameterSupport;

  private DefaultProblemBeanPostProcessor(DefaultBuilder builder) {
    this.problemFormat = builder.problemFormat;
    this.typeNameMapper = builder.typeNameMapper;
    this.bindingResultSupport = builder.bindingResultSupport;
    this.methodValidationResultSupport = builder.methodValidationResultSupport;
    this.methodParameterSupport = builder.methodParameterSupport;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    if (!(bean instanceof ProblemSupportAware)) {
      return bean;
    }

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
      Optional.ofNullable(problemFormat.get())
          .ifPresent(
              object -> {
                saveAuditLog(auditLog, object);
                aware.setProblemFormat(object);
              });
    }
  }

  private void maybeAddTypeNameMapper(Object bean, List<String> auditLog) {
    if (bean instanceof TypeNameMapperAware aware) {
      Optional.ofNullable(typeNameMapper.get())
          .ifPresent(
              object -> {
                saveAuditLog(auditLog, object);
                aware.setTypeNameMapper(object);
              });
    }
  }

  private void maybeAddBindingResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof BindingResultSupportAware aware) {
      Optional.ofNullable(bindingResultSupport.get())
          .ifPresent(
              object -> {
                saveAuditLog(auditLog, object);
                aware.setBindingResultSupport(object);
              });
    }
  }

  private void maybeAddMethodValidationResultSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodValidationResultSupportAware aware) {
      Optional.ofNullable(methodValidationResultSupport.get())
          .ifPresent(
              object -> {
                saveAuditLog(auditLog, object);
                aware.setMethodValidationResultSupport(object);
              });
    }
  }

  private void maybeAddMethodParameterSupport(Object bean, List<String> auditLog) {
    if (bean instanceof MethodParameterSupportAware aware) {
      Optional.ofNullable(methodParameterSupport.get())
          .ifPresent(
              object -> {
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

  static final class DefaultBuilder implements Builder {

    private Supplier<@Nullable ProblemFormat> problemFormat = () -> null;
    private Supplier<@Nullable TypeNameMapper> typeNameMapper = () -> null;
    private Supplier<@Nullable BindingResultSupport> bindingResultSupport = () -> null;
    private Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport =
        () -> null;
    private Supplier<@Nullable MethodParameterSupport> methodParameterSupport = () -> null;

    DefaultBuilder() {}

    @Override
    public Builder problemFormat(Supplier<@Nullable ProblemFormat> problemFormat) {
      this.problemFormat = requireNonNull(problemFormat, "problemFormat must not be null");
      return this;
    }

    @Override
    public Builder typeNameMapper(Supplier<@Nullable TypeNameMapper> typeNameMapper) {
      this.typeNameMapper = requireNonNull(typeNameMapper, "typeNameMapper must not be null");
      return this;
    }

    @Override
    public Builder bindingResultSupport(
        Supplier<@Nullable BindingResultSupport> bindingResultSupport) {
      this.bindingResultSupport =
          requireNonNull(bindingResultSupport, "bindingResultSupport must not be null");
      return this;
    }

    @Override
    public Builder methodValidationResultSupport(
        Supplier<@Nullable MethodValidationResultSupport> methodValidationResultSupport) {
      this.methodValidationResultSupport =
          requireNonNull(
              methodValidationResultSupport, "methodValidationResultSupport must not be null");
      return this;
    }

    @Override
    public Builder methodParameterSupport(
        Supplier<@Nullable MethodParameterSupport> methodParameterSupport) {
      this.methodParameterSupport =
          requireNonNull(methodParameterSupport, "methodParameterSupport must not be null");
      return this;
    }

    @Override
    public ProblemBeanPostProcessor build() {
      return new DefaultProblemBeanPostProcessor(this);
    }
  }
}
